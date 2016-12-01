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

(re-frame/reg-event-fx
 :login/success
 (fn [cofx]
   {:dispatch-n (list
                 [:game/get-friend-list]
                 [:active-page :create-game]
                 )}))
