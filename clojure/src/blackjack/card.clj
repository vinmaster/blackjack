(ns blackjack.card
  (:require [blackjack.utils :as utils]))

(def suits ["♠" "♦" "♣" "♥"])

(defn int->rank [x]
  (case x
    1 "A"
    11 "J"
    12 "Q"
    13 "K"
    (str x)))

(defn values
  "Possible values for given card"
  [card]
  (if (utils/number-str? (card :rank))
    [(Integer/parseInt (card :rank))]
    (case (card :rank)
      "A" [1 11]
      [10])))
