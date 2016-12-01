(ns trivia.events.create-game
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :as re-frame]
            [trivia.locations :as locations]))

(defn reset-game [db]
  (-> db
      (assoc-in [:state :round] 0)
      (assoc-in [:state :correct] 0)
      (assoc-in [:state :incorrect] 0)
      (assoc :answer-state :unknown)))

(re-frame/reg-event-db
 :game/select-player
 (fn [db [_ player]]
   (let [friends (:friends db)]
     (assoc db :friends 
            (map #(if (= player (:id %))
                    (update % :selected? not)
                    %)
                 friends)))))

(re-frame/reg-event-fx
 :game/get-questions-success
 (fn [{:keys [db]} [_ questions]]
   {:db (assoc db :questions questions)
    :dispatch-n (list [:game/next-question]
                      [:active-page :ask-question])
    }))

(re-frame/reg-event-fx
 :game/create-success
 (fn [{:keys [db]} [_ game]]
   {:db (assoc db :server-state game)
    :http-xhrio {:method :get
                 :uri (str locations/api "/games/" (:id game) "/questions")
                 :timeout 2000
                 :with-credentials true
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:game/get-questions-success]
                 :on-failure [:request-failure]}}))

(re-frame/reg-event-fx
 :game/create
 (fn [{:keys [db]} [_ players]]
   {:db (-> db (reset-game))
    :http-xhrio {:method :post
                 :uri (str locations/api "/games")
                 :params {:players players}
                 :timeout 2000
                 :format (ajax/json-request-format)
                 :with-credentials true
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:game/create-success]
                 :on-failure [:request-failure]}
    }))

(re-frame/reg-event-fx
 :game/friend-list-success
 (fn [{:keys [db]} [_ friends]]
   {:db (assoc db :friends friends)}))

(re-frame/reg-event-fx
 :game/get-friend-list
 (fn [{:keys [db]}]
   {:http-xhrio {:method :get
                 :uri (str locations/api "/users")
                 :timeout 2000
                 :with-credentials true
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:game/friend-list-success]
                 :on-failure [:request-failure]}
    }))
