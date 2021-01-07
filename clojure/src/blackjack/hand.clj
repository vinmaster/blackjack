(ns blackjack.hand
  (:require [blackjack.card :as card]))

(defn values [hand]
  (distinct (reduce (fn [acc card-values]
                      (for [value1 acc
                            value2 card-values]
                        (+ value1 value2)))
                    (map card/values (hand :cards)))))

(defn bust? [hand]
  (every? #(> %1 21) (values hand)))

(defn blackjack? [hand]
  (and (= 2 (count (hand :cards)))
       (= '(11 21) (values hand))))

(defn splittable? [hand]
  (and (= 2 (count (hand :cards)))
       (= ((first (hand :cards)) :rank) ((second (hand :cards)) :rank))))

(defn highest-value [hand]
  (apply max (filter #(<= %1 21) (conj (values hand) 0))))
