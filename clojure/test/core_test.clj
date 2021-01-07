(ns core-test
  (:require [clojure.test :refer :all])
  (:require [blackjack.core :as core]))

(deftest core-test
  (testing "should draw card from cards"
    (let [state (atom core/new-state)
          card {:suit "♠" :rank "A"}]
      ; Adds to cards
      (swap! state update-in [:cards] (comp vec concat) [card])
      (is (= (@state :cards) [card]))))

  (testing "should add card to player's hand"
    (let [state (atom core/new-state)
          card {:suit "♠" :rank "A"}]
      (swap! state update-in [:players] conj core/dealer)
      (is (= (get-in @state [:players 0 :hands]) []))
      (core/add-card-to-player-hand state 0 0 card)
      (is (= (get-in @state [:players 0 :hands]) [{:bet 0.0 :cards [card]}])))))
