(ns trivia.migrations
  (:require [clojure.java.jdbc :as j]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [migratus.core :as migratus]
            [taoensso.timbre :as log]
            [clojure.java.io :as io]))

(defn already-migrated? [db id]
  (= 1 (:migrated (first (j/query db ["select count(*) as migrated from puzzle_migrations where id=?" id])))))

(defn register-migration [db id]
  (j/execute! db ["insert into puzzle_migrations (id) values (?)" id ])
  )

(defn find-answer [db text]
  (first (j/query db ["select * from answer where text=?" text]))
  )

(defn insert-answer [db text]
  (j/execute! db ["insert into answer (text) values (?)" text])
  (:id (first (j/query db ["select id from answer where text = ?" text]))))

(defn add-answer
  "Add answer and return the id of the record"
  [db text]
  (let [answer (find-answer db text)]
    (if (nil? answer)
      (insert-answer db text)
      (:id answer))))

(defn link-answer [db puzzleid answer]
  (let [answerid (add-answer db (:text answer))]
    (j/execute! db ["insert into question_answer (question_id, answer_id, correct) values (?, ?, ?)"
                    puzzleid answerid (:correct? answer)])
    ))

(defn create-puzzle [db puzzle]
  (let [text (:question puzzle)
        answers (:answers puzzle)]
    (log/info "Creating puzzle" puzzle)
    (j/execute! db ["insert into question (text) values (?)" text])
    (let [id (:id (first (j/query db ["select id from question where text = ?" text])))]
      (doall (map #(link-answer db id %) answers)))
    )
  )

(defn process-set [db set]
  (let [setid (:set set)]
    (if (not (already-migrated? db setid))
      (do
        (log/info "Migrating puzzle set" setid)
        (doall (map #(create-puzzle db %) (:questions set)))
        (register-migration db setid))
      (log/info "Puzzle set" setid "already migrated"))))

(defn read-puzzle-data []
  (read-string (slurp (io/resource "puzzle-packs.edn"))))

(defn migrate-puzzles [db-spec]
  (log/info "Migrating puzzle database")
  (let [data (read-puzzle-data)]
    (doall (map #(process-set db-spec %) data))))

(defrecord Migrations [pool]
  component/Lifecycle
  (start [component]
    (log/info "Starting migrations")
    (let [config {:store                :database
                  :migration-dir        "migrations/"
                  :migration-table-name "schema_migrations"
                  :db (:spec pool)}]
      (log/info "Running migrations with spec: " config)
      (migratus/migrate config)
      (migrate-puzzles (:spec pool)))
    component)
  (stop [component]
    (log/info "Stopping migrations")
    component)
  )

(defn new-migrations [config-options]
  (map->Migrations config-options))

(comment
  ;; :db (:spec (component/start (trivia.connectionpool/new-connectionpool {})))
  (migratus/create  {:store                :database
                     :migration-dir        "migrations/"} "game_questions")

  (migratus/create  {:store                :database
                     :migration-dir        "migrations/"} "puzzle_migrations")

  (migratus/create  {:store                :database
                     :migration-dir        "migrations/"} "fix-data")
  
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
