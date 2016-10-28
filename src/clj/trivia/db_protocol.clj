(ns trivia.db-protocol)

(defprotocol UserActions
  "Protocol to access and manipulate users in the database"
  (get-user [this username]))

(defprotocol DbActions
  "Protocol that holds DB actions"
  (get-random-question [this])
  (correct-answer? [this questionid answerid]))
