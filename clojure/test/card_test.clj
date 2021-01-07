(ns card-test
  (:require [clojure.test :refer :all])
  (:require [blackjack.card :as card]))

(defn myfixture [block]
  (do
    (println "before test")
    (block)
    (println "after test")))

(use-fixtures :each myfixture)

(deftest card-test
  (testing "should turn numeric card value to rank representation"
    (is (= (card/int->rank 1) "A")))

  (testing "Ace should be values 1 and 11"
    (is (= (card/values {:suit "♠" :rank "A"}) [1, 11])))

  (testing "K should be value 10"
    (is (= (card/values {:suit "♠" :rank "K"}) [10])))

  (testing "5 should be value 5"
    (is (= (card/values {:suit "♠" :rank "5"}) [5]))))
