(ns trivia.db-protocol)

(defprotocol UserActions
  "Protocol to access and manipulate users in the database"
  (get-user [this username])
  (add-user [this username password])
  (get-friends [this username]))

(defprotocol GameActions
  "Protocol for actions related to Games"
  (create-game [this username players])
  (add-users-to-game [this game-id other-user])
  (list-games [this username])
  (get-game [this game-id username])
  (get-game-questions [this game-id username])
  (correct-answer? [this game-id questionid answerid username])
  (leaderboard [this game-id]))

(defprotocol DbActions
  "Protocol that holds DB actions"
  (get-random-question [this]))
