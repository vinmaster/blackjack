(ns blackjack.core
  (:require [clojure.string :as str])
  (:gen-class))

(def player-starting-cash 500)
(def number-of-decks 1)

(def new-state {:players []
                :cards []
                :quit false})

(def new-hand {:bet 0.0 :cards []})

(def state (atom new-state))

(defn user-input [] (read-line))

; (defn includes? [list val] (some #(= val %1) list))
(defn includes? [list val] (contains? (set list) val))

(defn prompt
  ([] (user-input))
  ([question]
   (print question)
   (flush)
   (user-input)))

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
  (let [name (prompt "What is your name? (Player): ")]
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
(defn shuffle-cards [state]
  (swap! state assoc-in [:cards] (shuffle deck)))

(defn draw-card [state]
  (when (zero? (count (@state :cards))) (shuffle-cards state))
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
  #_(repeatedly 2 (fn []
                    (doseq [[player-pos _] (map-indexed vector (@state :players))]
                      (add-card-to-player-hand state player-pos 0 (draw-card state)))))
  (dotimes [n 2]
    (doseq [[player-pos _] (map-indexed vector (@state :players))]
      (add-card-to-player-hand state player-pos 0 (draw-card state)))))

(defn display-player [player show-hidden]
  (println (player :name))
  (when-not (player :dealer) (println (format "Cash: $%.2f" (player :cash))))
  (doseq [hand (player :hands)]
    (->> (let [cards (map (fn [card] (str (card :rank) (card :suit))) (hand :cards))]
           (if (or (contains? player :cash) show-hidden)
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
                    (map card-values (hand :cards)))))

(defn hand-bust? [hand]
  (every? #(> %1 21) (hand-values hand)))

(defn hand-blackjack? [hand]
  (and (= 2 (count (hand :cards)))
       (= '(11 21) (hand-values hand))))

(defn hand-splittable? [hand]
  (and (= 2 (count (hand :cards)))
       (= ((first (hand :cards)) :rank) ((second (hand :cards)) :rank))))

(defn hand-highest-value [hand]
  (apply max (filter #(<= %1 21) (conj (hand-values hand) 0))))

(defn display-choices [hand hand-pos]
  (println (format "Choices for hand #%d:
  Hit (1 or h)
  Stand (2 or s)
  Double Down (3 or d)" hand-pos))
  (when (hand-splittable? hand) (println "  Split (4 or sp)\n")) (println "  Quit (0 or q)"))

(defn place-bets [state]
  (doseq [[player-pos player] (keep-indexed #(when-not (%2 :dealer) [%1 %2]) (@state :players))]
    (let [min 20.0
          max (double (player :cash))]

      (loop [bet-str (string-or-default (prompt (format "Wager amount. Between %.2f~%.2f (20): " min max)) "20")]

        (if-not (integer-str? bet-str)
          (do
            (println "Invalid number")
            (recur (string-or-default (prompt (format "Wager amount. Between %.2f~%.2f (20): " min max)) "20")))
          (let [bet (Double/parseDouble bet-str)]
            (if-not (<= min bet max)
              (do (println "Not in range") (recur (string-or-default (prompt (format "Wager amount. Between %.2f~%.2f (20): " min max)) "20")))
              (do
                (when (empty? (player :hands))
                  (swap! state update-in [:players player-pos :hands] conj new-hand))
                (swap! state update-in [:players player-pos :hands 0 :bet] + bet)
                (swap! state update-in [:players player-pos :cash] - bet)))))))

    ; Always bet $20
    #_(let [bet 20]
        (when (empty? (player :hands))
          (swap! state update-in [:players player-pos :hands] conj new-hand))
        (swap! state update-in [:players player-pos :hands 0 :bet] + bet)
        (swap! state update-in [:players player-pos :cash] - bet))))

(defn play-hand
  "Play hand and prompt for a choice. Returns true if this hand is done, false to continue playing this hand"
  [state player-pos hand-pos]
  (let [hand (get-in @state [:players player-pos :hands hand-pos])
        bet (get-in @state [:players player-pos :hands hand-pos :bet])]
    (display-state state false)
    (if (hand-blackjack? hand) true
        (do
          (display-choices hand hand-pos)
          (let [choice (string-or-default (prompt "Choose one (s): ") "s")]
            (cond
              (includes? ["1" "h"] choice)
              (do
                (add-card-to-player-hand state player-pos hand-pos (draw-card state))
                (hand-bust? (get-in @state [:players player-pos :hands hand-pos])))
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
                ; Remove the duplicate from original hand
                    (swap! state update-in [:players player-pos :hands hand-pos :cards] rest)
                    (add-card-to-player-hand state player-pos hand-pos (draw-card state))
                    (add-card-to-player-hand state player-pos next-hand-pos (draw-card state))
                    false)))
              (includes? ["0" "q"] choice)
              (do (swap! state assoc :quit true) false)
              :else false))))))

(defn play-round [state]
  (place-bets state)
  (deal-cards state)

  ; Players go first
  (doseq [[player-pos player] (keep-indexed #(when-not (%2 :dealer) [%1 %2]) (@state :players))]
    (loop [hand-pos 0] ; Loop over hands. Hands list might increase
      (when (and (not (@state :quit))
                 (< hand-pos (count (player :hands))))
        (let [done-playing-hand (play-hand state player-pos hand-pos)]
          (if done-playing-hand
            (recur (inc hand-pos))
            (recur hand-pos))))))
  ; Dealer goes last
  (let [[dealer-pos _] (first (keep-indexed #(when (%2 :dealer) [%1 %2]) (@state :players)))]
    (loop [dealer-hand (first (get-in @state [:players dealer-pos :hands]))]
      (when (and (< (hand-highest-value dealer-hand) 17)
                 (not (hand-bust? dealer-hand)))
        (add-card-to-player-hand state dealer-pos 0 (draw-card state))
        (recur (first (get-in @state [:players dealer-pos :hands]))))))
  ; Calculate round result
  (display-state state true)
  (doseq [[player-pos player] (keep-indexed #(when-not (%2 :dealer) [%1 %2]) (@state :players))]
    (let [[dealer-pos _] (first (keep-indexed #(when (%2 :dealer) [%1 %2]) (@state :players)))
          dealer-hand (first (get-in @state [:players dealer-pos :hands]))]
      (doseq [[hand-pos hand] (map-indexed vector (player :hands))]
        (cond
          (hand-blackjack? hand)
          (do
            (println (format "Hand: #%d has Blackjack! Winning $%.2f" hand-pos (* 2.5 (hand :bet))))
            (swap! state update-in [:players player-pos :cash] + (* 2.5 (hand :bet))))
          (hand-bust? hand)
          (println (format "Hand: #%d has busted. Losing $%.2f" hand-pos (hand :bet)))
          (hand-bust? dealer-hand)
          (do
            (println (format "Dealer busts. Winning $%.2f" (* 2 (hand :bet))))
            (swap! state update-in [:players player-pos :cash] + (* 2 (hand :bet))))
          (< (hand-highest-value dealer-hand)
             (hand-highest-value hand))
          (do
            (println (format "Dealer loses to Hand #%d. Wining $%.2f" hand-pos (* 2 (hand :bet))))
            (swap! state update-in [:players player-pos :cash] + (* 2 (hand :bet))))
          (> (hand-highest-value dealer-hand)
             (hand-highest-value hand))
          (println (format "Dealer wins over Hand #%d. Losing $%.2f" hand-pos (hand :bet)))
          (= (hand-highest-value dealer-hand)
             (hand-highest-value hand))
          (do
            (println (format "It is a push with Dealer. $%.2f is returned" (hand :bet)))
            (swap! state update-in [:players player-pos :cash] + (hand :bet)))))))
  ; Clear all hands and bets
  (doseq [[player-pos player] (map-indexed vector (@state :players))]
    (swap! state assoc-in [:players player-pos :hands] [])
    (when (and (contains? player :cash) (<= (player :cash) 0))
      (println "Player" (player :name) "ran out of cash")
      (swap! state assoc :quit true)))
  (println "----- End round -----\n"))

(defn play-game [state]
  (reset! state new-state)
  (shuffle-cards state)
  (let [player (setup-player)]
    (println (str "Welcome to Blackjack, " (player :name) "!"))
    (swap! state update-in [:players] conj dealer)
    (swap! state update-in [:players] conj player)
    (while (not (@state :quit)) (play-round state))))

(defn -main [& args] (play-game state))

;; (reset! state new-state)
;; (swap! state update-in [:players] conj dealer)
;; (swap! state update-in [:players] conj {:name "test" :hands [] :cash 500})
;; (deal-cards state)
;; (@state :players)

;; (shuffle-cards)
;; (swap! state assoc :quit true)
;; (add-card-to-player-hand state 1 0 (draw-card state))
;; (display-state state true)
;; (get-player-pos-by-name state "test")
