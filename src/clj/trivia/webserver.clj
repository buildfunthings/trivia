(ns trivia.webserver
  (:require [buddy.auth
             [accessrules :as baa]
             [middleware :as bam]]
            [buddy.auth.backends.session :as session]
            [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [org.httpkit.server :as http]
            [ring.middleware.session :as rms]
            [ring.util.http-response :as http-response]
            [taoensso.timbre :as log]))

(def cookie-name "trivia-session")

(def auth-backend (session/session-backend))

(defn wrap-app-session [handler]
  (-> handler
      (bam/wrap-authorization auth-backend)
      (bam/wrap-authentication auth-backend)
      (rms/wrap-session {:cookie cookie-name})))

(defn wrap-handler [handler]
  (-> handler
      (wrap-app-session)))

(defrecord WebServer [api host port server]
  component/Lifecycle
  (start [component]
    (log/info "Starting webserver http://" host ":" port)
    (assoc component :server (http/run-server (wrap-handler (:impl api)) {:host host :port port})))
  (stop [component]
    (log/info "Stopping webserver http://" host ":" port)
    (when-not (nil? server)
      (server :timeout 100))
    (assoc component :server nil)))

(defn new-webserver [config-options]
  (map->WebServer config-options))
