(ns trivia.rest-api-handler-test
  (:require [trivia.rest-api-handler :as sut]
            [clojure.test :as t]
            [clojure
             [string :as str]
             [test :as t :refer [deftest is testing report]]]
            [clojure.test.check :as tc]
            [clojure.test.check
             [clojure-test :as ct]
             [generators :as gen]
             [properties :as prop]]
            [ring.mock.request :as mock]
            [trivia
             [db-protocol :as db-protocol]]
            [taoensso.timbre :as log]))

;; Upper and lower case
(def char-accepted (gen/fmap char (gen/one-of
                                   [(gen/choose 97 122)
                                    (gen/choose 65 90)
                                    (gen/choose 48 59)
                                    ])))

;; Escaped regex chars
(def char-regex (gen/fmap char (gen/choose 33 47)))

(def char-regex-escaped (gen/fmap #(str  "\\" %) 
                                  (gen/vector char-regex)))
(def string-accepted (gen/fmap #(apply str %) 
                               (gen/vector char-accepted)))

(def csv-string (gen/fmap #(str/join "," %) 
                          (gen/vector string-accepted)))

(deftest build-cors-list 
  (is (= [] (sut/build-cors-list "")))
  (is (= [] (sut/build-cors-list nil)))
  (let [input "arjen,text"]
    (is (= (str/split input #",")
           (into [] (map #(.pattern %)
                         (sut/build-cors-list input)))))))

(ct/defspec csv-string-testing
  100
  (prop/for-all
   [s (gen/not-empty csv-string)]
   (= (str/split s #",")
      (into [] (map #(.pattern %)
                    (sut/build-cors-list s))))))

(defrecord TestFixture []
    db-protocol/DbActions
  (get-random-question [this]
    {:id 1234
     :question "How cool is ClojureScript?"
     :answers [{:id 1 :answer "Meh"}]})
  
  (correct-answer? [this question-id answer-id]
    true))

(defn new-testfixture []
  (map->TestFixture {}))

(deftest api-handler
  (is (= ((sut/handler nil) (mock/request :get "/not-found"))
         {:status 404,
          :headers {"Content-Type" "text/html; charset=utf-8"},
          :body "404 Not Found - oeps"}
         ))
  
  (let [{:keys [status body]} ((sut/handler (new-testfixture)) (mock/request :get "/api/question"))]
    ;;(log/info (slurp body))
    (is (= status 401))))


