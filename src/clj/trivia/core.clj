(ns trivia.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [trivia.system :as system]))

(def sys (system/system {:host (env :host) :port (Integer/parseInt (env :port "80"))}) )

(defn init
  "Creates and initializes the system under development in the Var
  #'system."
  []
  ;; TODO
  )

(defn start
  "Starts the system running, updates the Var #'system."
  []
  (alter-var-root #'sys component/start)
  )

(defn stop
  "Stops the system if it is currently running, updates the Var
  #'system."
  []
  (alter-var-root #'sys component/stop)
  )

(defn go
  "Initializes and starts the system running."
  []
  (init)
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after `go))

(defn -main
  "Start the application"
  [& args]
  (prn "Starting system")
  (component/start (system/system {:host (env :host) :port (Integer/parseInt (env :port "80"))})))
