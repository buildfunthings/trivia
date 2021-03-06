(ns trivia.rest-api-handler
  (:require [buddy
             [auth :as ba]
             [hashers :as bh]]
            [buddy.auth.accessrules :as baa]
            [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [compojure.api
             [meta :refer [restructure-param]]
             [sweet :refer :all]]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [re-frame.db :as db]
            [ring.middleware
             [cors :as cors]
             [logger :as logger]]
            [ring.util
             [http-response :refer :all]
             [response :as response]]
            [schema.core :as s]
            [taoensso.timbre :as log]
            [trivia
             [api :as api]
             [db-protocol :as db-protocol]
             [schema :as schema]]))

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
                     :tags [{:name "users", :descriptions "Functions dealing with users"}
                            {:name "games", :descriptions "Functions dealing with games"}]}}}
   (context "/api" []
            (POST "/login" []
                  :tags ["users"]
                  :body-params [username :- String, password :- String]
                  :summary "Logs a user in"
                  (if (api/authenticate-user db username password)
                    (assoc-in (ok {}) [:session :identity] {:username username})
                    (assoc-in (forbidden) [:session :identity] nil)))

            (GET "/login" []
                 :tags ["users"]
                 :auth-rules authenticated?
                 :current-user user
                 :return schema/User
                 (let [data (api/get-user-from-db db (:username user))
                       result (select-keys data [:id :username])]
                   (ok result)))
            
            (POST "/signup" []
                  :tags ["users"]
                  :body-params [username :- String, password :- String]
                  :summary "Sign an user up for our cool game."
                  (if (api/add-user-to-db db username (bh/derive password))
                    (assoc-in (ok {}) [:session :identity] {:username username})
                    (assoc-in (internal-server-error) [:session :identity] nil)))

            (GET "/users" []
                 :tags ["users"]
                 :auth-rules authenticated?
                 :current-user user
                 :return [schema/User]
                 (ok (api/get-friends db (:username user))))
            
            (POST "/games" []
                  :tags ["games"]
                  :body-params [players :- [Long]]
                  :auth-rules authenticated?
                  :current-user user
                  :return schema/Game
                  (ok (api/create-game db user players)))
            
            (GET "/games" []
                 :tags ["games"]
                 :auth-rules authenticated?
                 :current-user user
                 :return schema/OpenGames
                 (let [games (api/get-open-games db (:username user))]
                   (ok games)))

            (GET "/games/:id/questions" []
                 :tags ["games"]
                 :summary  "Retrieving list of questions for a specified game"
                 :path-params [id :- Long]
                 :auth-rules authenticated?
                 :current-user user
                 :return [schema/Question]
                 (ok (api/get-game-questions db id user)))

            (GET "/games/:game-id/leaderboard" []
                 :tags ["games"]
                 :summary  "Retrieving the leaderbord for a game"
                 :path-params [game-id :- Long]
                 :auth-rules authenticated?
                 :current-user user
                 :return schema/LeaderBoard
                 (ok (api/get-leaderboard db game-id)))
            
            (POST "/games/:game-id/verify/:question-id" []
                  :tags ["games"]
                  :return schema/QuestionAnswer
                  :auth-rules authenticated?
                  :current-user cur-user
                  :path-params [game-id :- s/Int
                                question-id :- s/Int]
                  :body [answer-id s/Int]
                  :summary "Return true or false for the answer"
                  (let [a (api/verify-answer db game-id question-id answer-id (:username cur-user))]
                    (ok {:correct? a})))
            
            (GET "/question" []
                 :tags ["deprecated"]
                 :return schema/Question
                 :auth-rules authenticated?
                 :current-user cur-user
                 :summary "Return a random question"
                 (ok (api/get-random-question db)))

            )))

(defn build-cors-list [input]
  (if (empty? input)
    []
    (into [] (map re-pattern (str/split input #",")))))

(defn handler [db]
  (-> (routes
       (app db)
       (GET "/" [] (response/redirect "/index.html"))
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
