(ns blackjack.utils
  (:require [clojure.string :as str]))

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

(defn find-indexes
  "Find indexes in collection (coll) that satify function (f)"
  [f coll] (keep-indexed #(when (f %2) %1) coll))

(defn number-str?
  "Checks if given string is a number"
  [s]
  (some? (re-matches #"^[+-]?\d+\.?\d*$" s)))
