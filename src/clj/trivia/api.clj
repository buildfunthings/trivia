(ns trivia.api
  (:require [buddy.hashers :as bh]
            [taoensso.timbre :as log]
            [trivia.db-protocol :as db-protocol]))

;;; Authentication
(defn get-user-from-db [db username]
  (db-protocol/get-user db username))

(defn authenticate-user
  "Authenticates the user, returning true if the username/password are valid
credentials."
  [db username password]
  (let [user (get-user-from-db db username)]
    (bh/check password (:hash user))))

(defn add-user-to-db [db username hash]
  (let [id (db-protocol/add-user db username hash)]
    (if (empty? id)
      false
      true)))

(defn get-friends [db username]
  (db-protocol/get-friends db username))

;;; Questions and games

(defn get-random-question
  "Retrieve a single, random, question from the database."
  [db]
  (db-protocol/get-random-question db))

(defn verify-answer
  "Check if the answer to a question is correct."
  [db game-id question-id answer-id username]
  (db-protocol/correct-answer? db game-id question-id answer-id username))

(defn- convert-dates [{:keys [date_started date_completed] :as  game}]
  ;;(prn date_started)
  (-> game
      (assoc :date_started (.format (java.text.SimpleDateFormat. "yyyy/MM/dd HH:mm:ss") date_started))
      (assoc :date_completed (if (nil? date_completed) ""
                                 (.format (java.text.SimpleDateFormat. "yyyy/MM/dd HH:mm:ss") date_completed)))))

(defn create-game
  "Create a new game and add the current user to it. If desired, multiple users can 
be added to the same game."
  [db {:keys [username] :as user} players]
  (let [game-id (db-protocol/create-game db username players)]
    (convert-dates (db-protocol/get-game db game-id username))))

(defn get-game-questions
  "Retrieve the set of questions that are part of a game identified by `id`"
  [db game-id {:keys [username]}]
  (db-protocol/get-game-questions db game-id username))

(defn get-open-games
  "Retrieve the list of open games for the `username`. Returns a map of maps keyed
by the `game_id`."
  [db username]
  (group-by :game_id  (db-protocol/get-open-games db username)))

(defn get-leaderboard
  "Retrieve a leaderboard for a game"
  [db game-id]
  (db-protocol/leaderboard db game-id))
