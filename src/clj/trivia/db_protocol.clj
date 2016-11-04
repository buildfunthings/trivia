(ns trivia.db-protocol)

(defprotocol UserActions
  "Protocol to access and manipulate users in the database"
  (get-user [this username])
  (add-user [this username password]))

(defprotocol DbActions
  "Protocol that holds DB actions"
  (get-random-question [this])
  (correct-answer? [this questionid answerid]))
