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
            [trivia.db-protocol :as db-protocol]
            [re-frame.db :as db]))

(s/defschema Game {:id s/Int ;; from game
                   ;; from game_user
                   :answered s/Int
                   :correct s/Int
                   ;; From game
                   :date_started s/Str
                   :date_completed s/Str
                   })

(s/defschema Answer {:id s/Int :answer s/Str})

(s/defschema Question {:id s/Int
                       :question s/Str
                       :answers [Answer]})

(s/defschema QuestionAnswer {:correct? s/Bool})

(defn get-random-question [db]
  (db-protocol/get-random-question db))

(defn get-user-from-db [db username]
  (db-protocol/get-user db username))

(defn add-user-to-db [db username hash]
  (let [id (db-protocol/add-user db username hash)]
    (if (empty? id)
      (assoc-in (internal-server-error) [:session :identity] nil)
      (assoc-in (ok {:id id}) [:session :identity] {:username username}))))

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

(defn convert-dates [{:keys [date_started date_completed] :as  game}]
  (prn date_started)
  (-> game
      (assoc :date_started (.format (java.text.SimpleDateFormat. "yyyy/MM/dd HH:mm:ss") date_started))
      (assoc :date_completed (if (nil? date_completed) ""
                                 (.format (java.text.SimpleDateFormat. "yyyy/MM/dd HH:mm:ss") date_completed)))))

(defn create-game [db {:keys [username] :as user}]
  (log/info "Creating new game for user" user)
  (let [game-id (db-protocol/create-game db username)]
    (log/info "Game created, id" game-id)
    (let [res (convert-dates (db-protocol/get-game db game-id username))]
      (log/info "Result " res)
      res)))

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
            
            (POST "/signup" []
                  :body-params [username :- String, password :- String]
                  :summary "Sign an user up for our cool game."
                  (add-user-to-db db username (bh/derive password)))

            (POST "/games" []
                  :auth-rules authenticated?
                  :current-user user
                  :return Game
                  (ok (create-game db user)))
            
            (GET "/games" []
                 :auth-rules authenticated?
                 :current-user user
                 :return [Game]
                 (log/info "Retrieving list of games for user" user)
                 (ok []))
            
            (GET "/question" []
                 :return Question
                 :auth-rules authenticated?
                 :current-user cur-user
                 :summary "Return a random question"
                 (ok (get-random-question db)))

            (POST "/question/:id" []
                  :return QuestionAnswer
                  :auth-rules authenticated?
                  :current-user cur-user
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
