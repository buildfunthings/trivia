(ns trivia.events.login
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :as re-frame]
            [trivia.locations :as locations]))

(re-frame/reg-event-fx
 :login/post
 (fn [cofx [event data]]
   {:db (assoc (:db cofx) :name data)
    :http-xhrio {:method :post
                 :uri (str locations/api "/login")
                 :with-credentials true
                 :params data
                 :format (ajax/json-request-format)
                 :timeout 2000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:login/success]
                 :on-failure [:request-failure]}
    }))

(re-frame/reg-event-db
 :login/get-user-success
 (fn [db [_ result]]
   (assoc db :user result)))

(re-frame/reg-event-fx
 :login/get-user
 (fn [{:keys [db]} event]
   {:http-xhrio {:method :get
                 :uri (str locations/api "/login")
                  :timeout 2000
                  :with-credentials true
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:login/get-user-success]
                  :on-failure [:request-failure]}}
   ))

(re-frame/reg-event-fx
 :login/success
 (fn [cofx]
   {:dispatch-n (list
                 [:login/get-user]
                 [:game/get-friend-list]
                 [:game/get-prev-games]
                 [:active-page :create-game]
                 )}))
