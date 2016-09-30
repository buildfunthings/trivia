(ns trivia.migrations
  (:require [com.stuartsierra.component :as component]
            [migratus.core :as migratus]
            [taoensso.timbre :as log]
            [environ.core :refer [env]]))

(defrecord Migrations [pool]
  component/Lifecycle
  (start [component]
    (let [config {:store                :database
                  :migration-dir        "migrations/"
                  :db (:spec pool)}]
      (migratus/migrate config))
    component)
  (stop [component]
    component)
  )

(defn new-migrations [config-options]
  (map->Migrations config-options))

(comment
  ;; :db (:spec (component/start (trivia.connectionpool/new-connectionpool {})))
  (migratus/create  {:store                :database
                     :migration-dir        "migrations/"} "question")

  (migratus/create  {:store                :database
                     :migration-dir        "migrations/"} "answer")

  (migratus/create  {:store                :database
                     :migration-dir        "migrations/"} "questions")
  
  (migratus/rollback {:store                :database
                      :migration-dir        "migrations/"
                      :db (:spec (component/start (trivia.connectionpool/new-connectionpool {})))
                      ;;(:spec (component/start (trivia.connectionpool/new-connectionpool {})))
                      ;; "postgres://postgres:password1@localhost:5432/trivia"
                      ;;(:spec (component/start (trivia.connectionpool/new-connectionpool {})))
                      })

  (migratus/migrate {:store                :database
                     :migration-dir        "migrations/"
                     :db (:spec (component/start (trivia.connectionpool/new-connectionpool {})))
                     ;;(:spec (component/start (trivia.connectionpool/new-connectionpool {})))
                     ;; "postgres://postgres:password1@localhost:5432/trivia"
                     ;;(:spec (component/start (trivia.connectionpool/new-connectionpool {})))
                     })
  )
