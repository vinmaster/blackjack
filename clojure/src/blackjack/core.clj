(ns blackjack.core
  (:require [clojure.string :as str])
  (:gen-class))

(def player-starting-cash 500)
(def number-of-decks 1)

(def new-state {:players []
                :cards []
                :quit false})

(def new-hand {:bet 0 :cards []})

(def state (atom new-state))

(defn user-input [] (read-line))

; (defn includes? [list val] (some #(= val %1) list))
(defn includes? [list val] (contains? (set list) val))

(defn prompt
  ([question]
   (print question)
   (flush)
   (user-input))
  ([] (user-input)))

(defn string-or-default [str default]
  (if (str/blank? str) default str))

(def suits ["♠" "♦" "♣" "♥"])

(defn int->rank [x]
  (case x
    1 "A"
    11 "J"
    12 "Q"
    13 "K"
    (str x)))

(def deck
  (for [suit suits
        i (range 13)
        :let [rank (int->rank (inc i))]]
    {:suit suit
     :rank rank}))

(defn setup-player []
  (let [name (prompt "What is your name?: ")]
    {:name (string-or-default name "Player")
     :hands []
     :cash player-starting-cash}))

(def dealer
  {:name "Dealer"
   :hands []
   :dealer true})

; Adds to cards
; (defn shuffle-deck []
;   (swap! state update-in [:cards] (comp vec concat) (shuffle deck)))

; Replace cards
(defn shuffle-cards []
  (swap! state assoc-in [:cards] (shuffle deck)))

(defn draw-card [state]
  (when (zero? (count (@state :cards))) (shuffle-cards))
  (let [[head & rest] (@state :cards)]
    (swap! state assoc-in [:cards] rest)
    head))

(defn find-indexes
  "Find indexes in collection (coll) that satify function (f)"
  [f coll] (keep-indexed #(when (f %2) %1) coll))

(defn get-player-pos-by-name [state player-name]
  (let [match-name #(= (%1 :name) player-name)]
    (first (find-indexes match-name (@state :players)))))

(defn add-card-to-player-hand
  "Player has list of hands. Add card to player name at hand position"
  [state player-pos hand-pos card]
  (let [hands (get-in @state [:players player-pos :hands])]
    (when (empty? hands)
      (swap! state update-in [:players player-pos :hands] conj new-hand))
    (swap! state update-in [:players player-pos :hands hand-pos :cards] conj card)))

(defn deal-cards [state]
  (repeatedly 2 (fn []
                  (doseq [player (@state :players)]
                    (add-card-to-player-hand state (player :name) 0 (draw-card state))))))

(defn display-player [player show-hidden]
  (println (player :name))
  (when-not (player :dealer) (println (format "Cash: $%d" (player :cash))))
  (doseq [hand (player :hands)]
    (->> (let [cards (map (fn [card] (str (card :rank) (card :suit))) (hand :cards))]
           (if show-hidden
             cards
             (into (rest cards) "?")))
         (str/join " ")
         (println)))
  (println))

(defn display-state [state show-hidden]
  (doseq [player (@state :players)] (display-player player show-hidden)))

(defn integer-str? [s]
  (some? (re-matches #"^[+-]?\d+\.?\d*$" s)))

(defn card-values [card]
  (if (integer-str? (card :rank))
    [(Integer/parseInt (card :rank))]
    (case (card :rank)
      "A" [1 11]
      [10])))

(defn hand-values [hand]
  (distinct (reduce (fn [acc card-values]
                      (for [value1 acc
                            value2 card-values]
                        (+ value1 value2)))
                    (map card-values hand))))

(defn hand-bust? [hand]
  (every? #(> %1 21) (hand-values hand)))

(defn hand-blackjack? [hand]
  (and (= 2 (count hand))
       (= '(11 21) (hand-values hand))))

(defn hand-splittable? [hand]
  (and (= 2 (count hand))
       (= ((first hand) :rank) ((second hand) :rank))))

(defn hand-highest-value [hand]
  (apply max (filter #(<= %1 21) (hand-values hand))))

(defn display-choices [hand]
  (println "Choices for hand #${i + 1}:
  Hit (1 or h)
  Stand (2 or s)
  Double Down (3 or d)")
  (when (hand-splittable? hand) (println "  Split (4 or sp)\n")) (println "  Quit (0 or q)"))

(defn play-hand
  "Play hand and prompt for a choice. Returns true if this hand is done, false to continue playing this hand"
  [state player-pos hand-pos]
  (let [hand (get-in @state [:players player-pos :hands hand-pos])
        bet (get-in @state [:players player-pos :hands hand-pos :bet])]
    (when-not (hand-blackjack? hand)
      (display-choices hand)
      (let [choice (string-or-default (prompt "Choose one (s): ") "s")]
        (cond
          (includes? ["1" "h"] choice)
          (do
            (add-card-to-player-hand state player-pos hand-pos (draw-card state))
            false)
          (includes? ["2" "s"] choice)
          true
          (includes? ["3" "d"] choice)
          (do
            (swap! state update-in [:players player-pos :cash] - bet)
            (swap! state update-in [:players player-pos :hands hand-pos :bet] * 2)
            (add-card-to-player-hand state player-pos hand-pos (draw-card state))
            true)
          (includes? ["4" "sp"] choice)
          (if-not (hand-splittable? hand)
            (do (println "Cannot split this hand") false)
            (do
              ; Add new hand
              (swap! state update-in [:players player-pos :hands] conj new-hand)
              (let [next-hand-pos (dec (count (get-in @state [:players 0 :hands])))]
                (swap! state update-in [:players player-pos :cash] - bet)
                (swap! state update-in [:players player-pos :hands next-hand-pos] + bet)
                ; Add last card from current hand to new hand
                (add-card-to-player-hand state player-pos next-hand-pos (peek (get-in @state [:players player-pos :hands hand-pos :cards])))
                (add-card-to-player-hand state player-pos hand-pos (draw-card state))
                (add-card-to-player-hand state player-pos next-hand-pos (draw-card state))
                false)))
          (includes? ["4" "sp"] choice)
          (do (swap! state assoc :quit true) false)
          :else false)))))

(defn play-round [state]
  ;place bets
  (deal-cards state)
  ; Players go first
  (doseq [[player-pos player] (keep-indexed #(not= (%2 :dealer) [%1 %2]) (@state :players))]
    (loop [hand-pos 0] ; Loop over hands. Hands list might increase
      (when-not (>= hand-pos (count (player :hands)))
        (play-hand state player-pos hand-pos)
        (recur (+ hand-pos 1)))))
  ; Dealer goes last
  (let [[dealer-pos _] (keep-indexed #(= (%2 :dealer) [%1 %2]) (@state :players))
        dealer-hand (get-in @state [:players dealer-pos])]
    (while (and (< hand-highest-value dealer-hand 17)
                (not (hand-bust? dealer-hand)))
      (add-card-to-player-hand state dealer-pos 0 (draw-card state))))
  ; Calculate round result
  (doseq [[player-pos player] (keep-indexed #(not= (%2 :dealer) [%1 %2]) (@state :players))
          [dealer-pos _] (keep-indexed #(= (%2 :dealer) [%1 %2]) (@state :players))
          dealer-hand (get-in @state [:players dealer-pos])]
    (doseq [[hand-pos hand] (player :hands)]
      (cond
        (hand-blackjack? hand)
        (do
          (println (format "Hand: #%d has Blackjack! Winning $%d" hand-pos (* 2.5 (hand :bet))))
          (swap! state update-in [:players player-pos :cash] + (* 2.5 (hand :bet))))
        (hand-bust? hand)
        (do
          (println (format "Hand: #%d has busted. Losing $%d" hand-pos (hand :bet))))
        (hand-bust? dealer-hand)
        (do
          (println (format "Dealer busts. Winning $%d" (* 2 (hand :bet))))
          (swap! state update-in [:players player-pos :cash] + (* 2 (hand :bet))))
        (< (hand-highest-value dealer-hand)
           (hand-highest-value hand))
        (do
          (println (format "Dealer loses to Hand #%d. Wining $%d" hand-pos (* 2 (hand :bet))))
          (swap! state update-in [:players player-pos :cash] + (* 2 (hand :bet))))
        (> (hand-highest-value dealer-hand)
           (hand-highest-value hand))
        (println (format "Dealer wins over Hand #%d. Losing $%d" hand-pos (hand :bet)))
        (= (hand-highest-value dealer-hand)
           (hand-highest-value hand))
        (do
          (println (format "It is a push with Dealer. $%d is returned" (hand :bet)))
          (swap! state update-in [:players player-pos :cash] + (hand :bet))))))
  ; Clear all hands and bets
  (doseq [[player-pos player] (map-indexed #([%1 %2]) (@state :players))]
    (swap! state assoc-in [:players player-pos :hands] [])
    (when (< (player :cash) 0)
      (swap! state assoc :quit true))))

(defn play-game [state]
  (reset! state new-state)
  ;while not quit
  (while (not (@state :quit)) (play-round state))
  )

(defn -main [& args]
  (let [player (setup-player)]
    (println (str "Name: " (player :name)))
    (play-game state)))

;; (shuffle-cards)
;; (swap! state update-in [:players] conj dealer)
;; (swap! state update-in [:players] conj {:name "test" :hands [] :cash 500})
;; (swap! state assoc-in [:players] [])
;; (swap! state assoc :quit true)
;; (add-card-to-player-hand state 1 0 (draw-card state))
;; (count (@state :cards))
;; (deal-cards state)
;; (@state :players)
;; (display-state state true)
;; (display-player (last (@state :players)) false)
;; (get-player-pos-by-name state "test")
;; (count (get-in @state [:players 0 :hands]))
