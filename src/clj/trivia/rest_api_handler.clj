(ns trivia.rest-api-handler
  (:require [com.stuartsierra.component :as component]
            [compojure.api.sweet :refer :all]
            [environ.core :refer [env]]
            [compojure.route :as route]
            [ring.middleware
             [cors :as cors]
             [logger :as logger]]
            [schema.core :as s]
            [trivia.db-protocol :as db-protocol]
            [ring.util.http-response :refer :all]
            [taoensso.timbre :as log]

            [clojure.string :as str]))

(s/defschema Answer {:id s/Int :answer s/Str})

(s/defschema Question {:id s/Int
                       :question s/Str
                       :answers [Answer]})

(s/defschema QuestionAnswer {:correct? s/Bool})

(defn get-random-question [db]
  (db-protocol/get-random-question db))

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
                 (ok (get-random-question db)))

            (POST "/question/:id" []
                  :return QuestionAnswer
                  :path-params [id :- s/Int]
                  :body [answer-id s/Int]
                  :summary "Return true or false for the answer"
                  (let [a (db-protocol/correct-answer? db id answer-id)]
                    (ok {:correct? a}))
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

(defrecord RestApiHandler [db]
  component/Lifecycle
  (start [component]
    (assoc component :impl (handler db)))
  (stop [component]
    component))

(defn new-restapihandler [config-options]
  (map->RestApiHandler config-options))
