(ns trivia.in-memory-db
  (:require [com.stuartsierra.component :as component]
            [trivia.db-protocol :as db-protocol]))

(def db [{:id 1234
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
                    {:id 4 :answer "Danish" :correct false}]}
         {:id 2000
          :question "Which of these former U.S. Presidents appeared on the television series Laugh-In?"
          :answers [{:id 1 :answer "Lyndon Johnson" :correct false}
                    {:id 2 :answer "Jimmy Carter" :correct false}
                    {:id 3 :answer "Richard Nixon" :correct true}
                    {:id 4 :answer "Gerald Ford" :correct false}]}
         {:id 2001
          :question "The Earth is approximately how many miles away from the Sun?"
          :answers [{:id 1 :answer "9.3 million" :correct false}
                    {:id 2 :answer "93 million" :correct true}
                    {:id 3 :answer "39 million" :correct true}
                    {:id 4 :answer "193 million" :correct false}]}
         {:id 2002
          :question "Which insect shorted out an early supercomputer and inspired the term 'computer bug'?"
          :answers [{:id 1 :answer "Moth" :correct true}
                    {:id 2 :answer "Fly" :correct false}
                    {:id 3 :answer "Roach" :correct false}
                    {:id 4 :answer "Japanese beetle" :correct false}]}
         {:id 2003
          :question "Which of the following men does not have a chemical element named after him?"
          :answers [{:id 1 :answer "Albert Einstein" :correct false}
                    {:id 2 :answer "Isaac Newton" :correct true}
                    {:id 3 :answer "Niels Bohr" :correct false}
                    {:id 4 :answer "Enrico Fermi" :correct false}]}
         {:id 2004
          :question "Which of the following landlocked countries is entirely contained within another country?"
          :answers [{:id 1 :answer "Lesotho" :correct true}
                    {:id 2 :answer "Mongolia" :correct false}
                    {:id 3 :answer "Burkina Faso" :correct false}
                    {:id 4 :answer "Luxembourg" :correct false}]}
         {:id 2005
          :question "In the children's book series, where is Paddington Bear originally from?"
          :answers [{:id 1 :answer "India" :correct false}
                    {:id 2 :answer "Canada" :correct false}
                    {:id 3 :answer "Peru" :correct true}
                    {:id 4 :answer "Iceland" :correct false}]}
         {:id 2006
          :question "Who is credited with inventing the first mass-produced helicopter?"
          :answers [{:id 1 :answer "Igor Sikorsky" :correct true}
                    {:id 2 :answer "Ferdinand von Zeppelin" :correct false}
                    {:id 3 :answer "Elmer Sperry" :correct false}
                    {:id 4 :answer "Gottlieb Daimler" :correct false}]}
         {:id 2007
          :question "According to the Population Reference Bureau, what is the approximate number of people who have ever lived on Earth?"
          :answers [{:id 1 :answer "50 Billion" :correct false}
                    {:id 2 :answer "1 Trillion" :correct false}
                    {:id 3 :answer "100 Billion" :correct true}
                    {:id 4 :answer "5 Trillion" :correct false}]}])

(defrecord InMemoryDb []
  db-protocol/DbActions
  (get-random-question [this]
    (rand-nth db))

  (correct-answer? [this question-id answer-id]
    (let [question (first (filter #(= (:id %) question-id) db))
          answer (first (filter #(= (:id %) answer-id) (:answers question)))]
      (if (nil? answer)
        false
        (:correct answer))))
  
  component/Lifecycle
  (start [component]
    component)
  (stop [component]
    component))

(defn new-inmemorydb [config-options]
  (map->InMemoryDb config-options))
