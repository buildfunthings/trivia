(ns trivia.webserver
  (:require [com.stuartsierra.component :as component]
            [compojure.api.sweet :refer :all]
            [org.httpkit.server :as http]
            [ring.middleware.cors :as cors]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
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

(defn handler [db]
  (-> (app db)
      (cors/wrap-cors :access-control-allow-origin [#"http://localhost:3449"]
                      :access-control-allow-methods [:get :post] ) ))

(defrecord WebServer [db port server]
  component/Lifecycle
  (start [component]
    (assoc component :server (http/run-server (handler db) {:port port})))
  (stop [component]
    (when-not (nil? server)
      (server :timeout 100))
    (assoc component :server nil)))

(defn new-webserver [config-options]
  (map->WebServer config-options))
