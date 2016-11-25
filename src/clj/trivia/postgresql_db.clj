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

(defn- verify-answer [db game-id question-id answer-id username]
  (let [{:keys [correct]} (is-answer-correct? db
                                              {:question_id question-id :answer_id answer-id})]
    (db-update-player-meta db {:username username
                               :game_id game-id
                               :nr_answered 1
                               :nr_correct (if correct 1 0)})
    (if (nil? correct) false correct)))

(defn- get-leaderboard [db game-id]
  (apply list (db-get-leader-board db {:game_id game-id})))

(defn- create-game [{:keys [spec]} username players]
  (let [game-id (:id (first (db-create-game spec)))]
    (associate-questions spec {:game_id game-id})
    (db-add-user-to-game spec {:game_id game-id :username username})
      (if-not (empty? players)
        (doall (map #(db-add-user-to-game-by-id spec {:game_id game-id :user_id %}) players)))
      game-id))

(defrecord PostgreSQL-DB [pool]
  db-protocol/DbActions
  (get-random-question [this]
    (create-question (get-question-data (:spec pool) (get-random-question-id (:spec pool)))))

  db-protocol/GameActions
  (create-game [this username players]
    (create-game pool username players))
  
  (add-users-to-game [this game-id other-user])
  (list-games [this username])
  
  (get-game [this game-id username]
    (db-get-game (:spec pool) {:game_id game-id :username username}))

  (get-game-questions [this game-id username]
    (get-game-questions-fn (:spec pool) game-id username))

  (correct-answer? [this game-id question-id answer-id username]
    (verify-answer (:spec pool) game-id question-id answer-id username))

  (leaderboard [this game-id]
    (get-leaderboard (:spec pool) game-id))
  
  db-protocol/UserActions
  (get-user [this username]
    (get-user-by-name (:spec pool) {:username username}))

  (add-user [this username hash]
    (add-user (:spec pool) {:username username :hash hash}))

  (get-friends [this username]
    (db-get-friends (:spec pool) {:username username}))
  
  component/Lifecycle
  (start [component]
    component)
  (stop [component]
    component)
  )

(defn new-postresql-db [config-options]
  (map->PostgreSQL-DB config-options))






