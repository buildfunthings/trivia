(ns trivia.db
  (:require [re-frame.core :as re-frame]))

(def default-value
  {:name "Arjen"

   :state {:round 1
           :max-rounds 5
           :correct 0
           :incorrect 0}
   
   :active-page :login
   :answer-state :unknown
   
   :questions [{:id 1234
                :question "How cool is ClojureScript?"
                :answers [{:id 1 :answer "Meh" :correct false}
                          {:id 2 :answer "It's ok" :correct false}
                          {:id 3 :answer "(awesome \"it is\")" :correct true}
                          {:id 4 :answer "Rubbish" :correct false}]}
               {:id 1235
                :question "What city is in The Netherlands?"
                :answers [{:id 1 :answer "San Francisco" :correct false}
                          {:id 2 :answer "Singapore" :correct false}
                          {:id 3 :answer "Barcelona" :correct false}
                          {:id 4 :answer "Amsterdam" :correct true}]}
               {:id 1236
                :question "What language do they speak in The Netherlands?"
                :answers [{:id 1 :answer "English" :correct false}
                          {:id 2 :answer "Dutch" :correct true}
                          {:id 3 :answer "Frysian" :correct true}
                          {:id 4 :answer "Danish" :correct false}]}]})
