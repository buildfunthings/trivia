(ns trivia.system
  (:require [com.stuartsierra.component :as component]
            [trivia
             [connectionpool :as pool]
             [in-memory-db :as in-memory-db]
             [migrations :as migrations]
             [webserver :as webserver]]
            [taoensso.timbre :as log]))

(defn system [config-options]
  (log/info "Creating system")
  (let [{:keys [port]} config-options]
    (component/system-map
     :pool (pool/new-connectionpool config-options)
     :migrations (component/using
                  (migrations/new-migrations config-options)
                  [:pool])
     :db (component/using
          (in-memory-db/new-inmemorydb config-options)
          [:pool :migrations])
     :http (component/using
            (webserver/new-webserver config-options)
            [:db]))))
