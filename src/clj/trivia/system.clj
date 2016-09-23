(ns trivia.system
  (:require [com.stuartsierra.component :as component]
            [trivia
             [in-memory-db :as in-memory-db]
             [webserver :as webserver]]))

(defn system [config-options]
  (let [{:keys [port]} config-options]
    (component/system-map
     :db (in-memory-db/new-inmemorydb config-options)
     :http (component/using
            (webserver/new-webserver config-options)
            [:db]))))
