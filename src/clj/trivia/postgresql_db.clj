(ns trivia.postgresql-db
  (:require [com.stuartsierra.component :as component]
            [hugsql.core :as hugsql]
            [trivia.db-protocol :as db-protocol]
            [taoensso.timbre :as log]))

(hugsql/def-db-fns "queries.sql")

(defn create-answer [entry]
  {:id (:answer_id entry) :answer (:answer_text entry)})

(defn create-question [data]
  (let [{:keys [question_id question_text]} (first data)
        answers (into [] (map #(create-answer %) data))]
    {:id question_id :question question_text :answers answers}))

(defn process-question [[key questions]]
  (create-question questions))

(defn get-game-questions-fn [db game-id username]
  (let [data  (get-questions-for-game db {:game_id game-id :username username})]
    (map #(process-question %) (group-by :question_id data))))

(comment

  (def d '({:question_id 18, :question_text "The Moon of Barods, a diamond that Marilyn Monroe wore when singing \"Diamonds Are A Girl's Best Friend\" in the film Gentlemen prefer Blondes, was auctioned off at Christies for how much in 1990?", :answer_id 68, :answer_text "$97,000"} {:question_id 18, :question_text "The Moon of Barods, a diamond that Marilyn Monroe wore when singing \"Diamonds Are A Girl's Best Friend\" in the film Gentlemen prefer Blondes, was auctioned off at Christies for how much in 1990?", :answer_id 69, :answer_text "$297,000"} {:question_id 18, :question_text "The Moon of Barods, a diamond that Marilyn Monroe wore when singing \"Diamonds Are A Girl's Best Friend\" in the film Gentlemen prefer Blondes, was auctioned off at Christies for how much in 1990?", :answer_id 70, :answer_text "$497,000"} {:question_id 18, :question_text "The Moon of Barods, a diamond that Marilyn Monroe wore when singing \"Diamonds Are A Girl's Best Friend\" in the film Gentlemen prefer Blondes, was auctioned off at Christ
ies for how much in 1990?", :answer_id 71, :answer_text "$797,000"} {:question_id 22, :question_text "Clint Eastwood gave us the immortal line, \"Go ahead... make my day\", in what film?", :answer_id 84, :answer_text "Dirty Harry"} {:question_id 22, :question_text "Clint Eastwood gave us the immortal line, \"Go ahead... make my day\", in what film?", :answer_id 85, :answer_text "Magnum Force"} {:question_id 22, :question_text "Clint Eastwood gave us the immortal line, \"Go ahead... make my day\", in what film?", :answer_id 86, :answer_text "Sudden Impact"} {:question_id 22, :question_text "Clint Eastwood gave us the immortal line, \"Go ahead... make my day\", in what film?", :answer_id 87, :answer_text "Tightrope"} {:question_id 24, :question_text "How cool is ClojureScript?", :answer_id 91, :answer_text "Meh"} {:question_id 24, :question_text "How cool is ClojureScript?", :answer_id 92, :answer_text "It's ok"} {:question_id 24, :question_text "How cool is ClojureScript?", :answer_id 93, :answer_text "(awesom
e \"it is\")"} {:question_id 24, :question_text "How cool is ClojureScript?", :answer_id 94, :answer_text "Rubbish"} {:question_id 25, :question_text "What city is in The Netherlands?", :answer_id 95, :answer_text "San Francisco"} {:question_id 25, :question_text "What city is in The Netherlands?", :answer_id 96, :answer_text "Singapore"} {:question_id 25, :question_text "What city is in The Netherlands?", :answer_id 97, :answer_text "Barcelona"} {:question_id 25, :question_text "What city is in The Netherlands?", :answer_id 98, :answer_text "Amsterdam"} {:question_id 31, :question_text "Which of the following landlocked countries is entirely contained within another country?", :answer_id 119, :answer_text "Lesotho"} {:question_id 31, :question_text "Which of the following landlocked countries is entirely contained within another country?", :answer_id 120, :answer_text "Mongolia"} {:question_id 31, :question_text "Which of the following landlocked countries is entirely contained within another country?", :answer_id
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               121, :answer_text "Burkina Faso"} {:question_id 31, :question_text "Which of the following landlocked countries is entirely contained within another country?", :answer_id 122, :answer_text "Luxembourg"}))

  (map #(prn (:question_id %)) d)

  (def s  (group-by :question_id d))

  s
  (map #(process-question %) s)
  {:QI {QS [AS]}}
  
  - recurse over list of ids
    - filter questions based on id
    - call create-question on the filtered result
  
  )

(defrecord PostgreSQL-DB [pool]
  db-protocol/DbActions
  (get-random-question [this]
    (create-question (get-question-data (:spec pool) (get-random-question-id (:spec pool)))))
  
  (correct-answer? [this question-id answer-id]
    (let [correct (:correct (is-answer-correct? (:spec pool)
                                                {:question_id question-id :answer_id answer-id}))]
      (if (nil? correct) false correct)))

  db-protocol/GameActions
  (create-game [this username]
    (let [game-id (:id (first (db-create-game (:spec pool))))]
      (associate-questions (:spec pool) {:game_id game-id})
      (db-add-user-to-game (:spec pool) {:game_id game-id :username username})
      game-id))
  
  (add-users-to-game [this game-id other-user])
  (list-games [this username])
  
  (get-game [this game-id username]
    (db-get-game (:spec pool) {:game_id game-id :username username}))

  (get-game-questions [this game-id username]
    (get-game-questions-fn (:spec pool) game-id username))
  
  db-protocol/UserActions
  (get-user [this username]
    (get-user-by-name (:spec pool) {:username username}))

  (add-user [this username hash]
    (add-user (:spec pool) {:username username :hash hash}))
  
  component/Lifecycle
  (start [component]
    component)
  (stop [component]
    component)
  )

(defn new-postresql-db [config-options]
  (map->PostgreSQL-DB config-options))






