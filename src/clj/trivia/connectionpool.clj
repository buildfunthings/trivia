(ns trivia.connectionpool
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [jdbc.pool.c3p0 :as pool]
            [taoensso.timbre :as log])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

(defrecord ConnectionPool [spec]
  component/Lifecycle
  (start [component]
    (let [uri (java.net.URI. (env :database-url))
          [username password] (str/split (.getUserInfo uri) #":")]
      (assoc component :spec  (pool/make-datasource-spec
                               {:classname "org.postgresql.Driver"
                                :subprotocol "postgresql"
                                :user username
                                :password password
                                :subname (if (= -1 (.getPort uri))
                                           (format "//%s%s" (.getHost uri) (.getPath uri))
                                           (format "//%s:%s%s" (.getHost uri) (.getPort uri)
                                                   (.getPath uri)))}))))
  (stop [component]
    (log/info "Current spec: " spec)
    (when-not (nil? spec)
      (.close spec))
    (dissoc component :spec)))

(defn new-connectionpool [config-options]
  (log/info "Creating new connection pool")
  (map->ConnectionPool config-options))
