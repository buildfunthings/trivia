(ns trivia.system
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [trivia
             [connectionpool :as pool]
             [in-memory-db :as in-memory-db]
             [migrations :as migrations]
             [postgresql-db :as postgresql-db]
             [rest-api-handler :as rest-api]
             [webserver :as webserver]]))

(defn system [config-options]
  (log/info "Creating system")
  (let [{:keys [port]} config-options]
    (component/system-map
     :pool (pool/new-connectionpool config-options)
     :migrations (component/using
                  (migrations/new-migrations config-options)
                  [:pool])
     :db (component/using
          (postgresql-db/new-postresql-db config-options)
          [:pool :migrations])
     :api (component/using
           (rest-api/new-restapihandler config-options)
           [:db])
     :http (component/using
            (webserver/new-webserver config-options)
            [:api]))))
