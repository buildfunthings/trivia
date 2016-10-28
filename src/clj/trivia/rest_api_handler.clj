(ns trivia.rest-api-handler
  (:require [buddy
             [auth :as ba]
             [hashers :as bh]]
            [buddy.auth.accessrules :as baa]
            [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [compojure.api.sweet :refer :all]
            [compojure.api.meta :refer [restructure-param]]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [ring.middleware
             [cors :as cors]
             [logger :as logger]]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [taoensso.timbre :as log]
            [trivia.db-protocol :as db-protocol]))

(s/defschema Answer {:id s/Int :answer s/Str})

(s/defschema Question {:id s/Int
                       :question s/Str
                       :answers [Answer]})

(s/defschema QuestionAnswer {:correct? s/Bool})

(defn get-random-question [db]
  (db-protocol/get-random-question db))

(defn get-user-from-db [db username]
  (db-protocol/get-user db username))

(defn access-error [request value]
  (unauthorized value))

(defn wrap-rule [handler rule]
  (-> handler
      (baa/wrap-access-rules {:rules [{:pattern #".*"
                                       :handler rule}]
                              :on-error access-error})))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-rule rule]))

(defmethod restructure-param :current-user
  [_ binding acc]
  (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

(defn authenticated? [req]
  (ba/authenticated? req))

(defn app [db]
  (api
   {:swagger {:ui "/api-docs"
              :spec "/swagger.json"
              :data {:info {:title "Trivia API"
                            :description "API for our Trivia game"}
                     :tags [{:name "questions", :descriptions "Functions dealing with questions"}]}}}
   (context "/api" []
            :tags ["question"]

            (POST "/login" []
                  :body-params [username :- String, password :- String]
                  :summary "Logs a user in"
                  (let [user (get-user-from-db db username)]
                    (if (bh/check password (:hash user))
                      (assoc-in (ok {}) [:session :identity] {:username (:username user)})
                      (assoc-in (forbidden) [:session :identity] nil)
                      ))
                  )

            (GET "/question" []
                 :return Question
                 :auth-rules authenticated?
                 :current-user cur-user
                 :summary "Return a random question"
                 (log/info "User requesting a question" (:username cur-user))
                 (ok (get-random-question db)))

            (POST "/question/:id" []
                  :return QuestionAnswer
                  :auth-rules authenticated?
                  :current-user cur-user
                  :path-params [id :- s/Int]
                  :body [answer-id s/Int]
                  :summary "Return true or false for the answer"
                  (let [a (db-protocol/correct-answer? db id answer-id)]
                    (log/info "User" (:username cur-user) "provided an answer and it was" a)
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
                      :access-control-allow-methods [:get :post :options]
                      :access-control-allow-credentials true) ))

(defrecord RestApiHandler [db]
  component/Lifecycle
  (start [component]
    (assoc component :impl (handler db)))
  (stop [component]
    component))

(defn new-restapihandler [config-options]
  (map->RestApiHandler config-options))
