(ns trivia.in-memory-db-test
  (:require [trivia.in-memory-db :as sut]
            [clojure.test :as t :refer [deftest testing is]]))

(deftest sanitizers
  (testing "Answer sanitizer"
    (is (= [{:id 1 :answer "123"}]
           (sut/sanitize-answers [{:id 1 :answer "123" :correct? true}]))))
  (testing "Question sanitizer"
    (is (= {:id 1234
            :question "How cool is ClojureScript?"
            :answers [{:id 1 :answer "Meh"}
                      {:id 2 :answer "It's ok"}
                      {:id 3 :answer "(awesome \"it is\")"}
                      {:id 4 :answer "Rubbish"}]}
           (sut/sanitize-question {:id 1234
                                   :question "How cool is ClojureScript?"
                                   :answers [{:id 1 :answer "Meh" :correct false}
                                             {:id 2 :answer "It's ok" :correct false}
                                             {:id 3 :answer "(awesome \"it is\")" :correct true}
                                             {:id 4 :answer "Rubbish" :correct false}]})))
    )
  )
