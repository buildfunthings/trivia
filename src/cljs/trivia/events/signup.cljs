(ns trivia.events.signup
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :as re-frame]
            [trivia.locations :as locations]))

(re-frame/reg-event-fx
 :signup/navigate
 (fn [cofx]
   {:dispatch [:active-page :signup]}))

(re-frame/reg-event-fx
 :signup/perform
 (fn [cofx [event username password]]
   {:http-xhrio {:method :post
                 :uri (str locations/api "/signup")
                 :params {:username username
                          :password password}
                 :timeout 2000
                 :format (ajax/json-request-format)
                 :with-credentials true
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:login/success]
                 :on-failure [:request-failure]}}))
