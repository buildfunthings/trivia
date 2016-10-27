(ns trivia.webserver
  (:require [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [org.httpkit.server :as http]
            [taoensso.timbre :as log]
            ))

(defrecord WebServer [api host port server]
  component/Lifecycle
  (start [component]
    (log/info "Starting webserver http://" host ":" port)
    (assoc component :server (http/run-server (:impl api) {:host host :port port})))
  (stop [component]
    (log/info "Stopping webserver http://" host ":" port)
    (when-not (nil? server)
      (server :timeout 100))
    (assoc component :server nil)))

(defn new-webserver [config-options]
  (map->WebServer config-options))
