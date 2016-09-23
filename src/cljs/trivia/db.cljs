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
                              ]})
