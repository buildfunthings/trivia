(ns trivia.core-test
  (:require [trivia.core :as sut]
            [clojure.test :as t]))

(t/deftest testing-add
  (t/is (= (sut/add-numbers 1 2) 3)))

