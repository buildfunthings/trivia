(ns trivia.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [trivia.system :as system]
            [com.stuartsierra.component :as component]))

(def sys (system/system {:port 8080}) )

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
  (println "Hello World"))
