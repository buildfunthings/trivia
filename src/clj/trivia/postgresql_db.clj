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






