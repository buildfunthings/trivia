(ns trivia.webserver
  (:require [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [compojure.api.sweet :refer :all]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [org.httpkit.server :as http]
            [ring.middleware
             [cors :as cors]
             [logger :as logger]]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [taoensso.timbre :as log]
            [trivia.db-protocol :as db-protocol]))

(s/defschema Answer {:id s/Int :answer s/Str :correct s/Bool})

(s/defschema Question {:id s/Int
                       :question s/Str
                       :answers [Answer]})

(s/defschema QuestionAnswer {:correct? s/Bool})

(defn app [db]
  (api
   {:swagger {:ui "/api-docs"
              :spec "/swagger.json"
              :data {:info {:title "Trivia API"
                            :description "API for our Trivia game"}
                     :tags [{:name "questions", :descriptions "Functions dealing with questions"}]}}}
   (context "/api" []
            :tags ["question"]

            (GET "/question" []
                 :return Question
                 :summary "Return a random question"
                 (ok (db-protocol/get-random-question db)))

            (POST "/question/:id" []
                  :return QuestionAnswer
                  :path-params [id :- s/Int]
                  :body [answer-id s/Int]
                  :summary "Return true or false for the answer"
                  (ok {:correct? (db-protocol/correct-answer? db id answer-id)})
                  ))))

(defn build-cors-list [input]
  (if (empty? input)
    []
    (into [] (map re-pattern (str/split input #",")))))

(defn handler [db]
  (-> (routes
       (app db)
       (route/resources "/")
       (route/not-found "404 Not Found - oeps"))
;;      (logger/wrap-with-logger :info (fn [x] (prn x)))
      (cors/wrap-cors :access-control-allow-origin (build-cors-list (env :cors))
                      :access-control-allow-methods [:get :post :options] ) ))

(defrecord WebServer [db host port server]
  component/Lifecycle
  (start [component]
    (log/info "Starting webserver http://" host ":" port)
    (assoc component :server (http/run-server (handler db) {:host host :port port})))
  (stop [component]
    (log/info "Stopping webserver http://" host ":" port)
    (when-not (nil? server)
      (server :timeout 100))
    (assoc component :server nil)))

(defn new-webserver [config-options]
  (map->WebServer config-options))
