(ns trivia.schema
  (:require [schema.core :as s]))


(s/defschema User {:id s/Int
                   :username s/Str})

(s/defschema PlayerMeta {:id s/Int
                         :username s/Str
                         :answered s/Int
                         :correct s/Int})

(s/defschema LeaderBoard [PlayerMeta])

(s/defschema Game {:id s/Int ;; from game
                   ;; from game_user
                   :answered s/Int
                   :correct s/Int
                   ;; From game
                   :date_started s/Str
                   :date_completed s/Str
                   })

(s/defschema PlayerStatus {:game_id s/Int
                           :user_id s/Int
                           :answered s/Int
                           :correct s/Int})

(s/defschema OpenGames {s/Int
                        [PlayerStatus]
                        })

;; (s/validate OpenGames 
;;             {1 [{:game_id 1 :user_id 1 :answered 3 :correct 1}]})

(s/defschema Answer {:id s/Int :answer s/Str})

(s/defschema Question {:id s/Int
                       :question s/Str
                       :answers [Answer]})

(s/defschema QuestionAnswer {:correct? s/Bool})
