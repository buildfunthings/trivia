(ns trivia.db-protocol)

(defprotocol DbActions
  "Protocol that holds DB actions"
  (get-random-question [this])
  (correct-answer? [this questionid answerid]))
