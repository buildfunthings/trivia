(ns trivia.events.end-game
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :as re-frame]
            [trivia.locations :as locations]))

(re-frame/reg-event-fx
 :game/get-leaderboard-success
 (fn [{:keys [db]} [_ leaderboard]]
   {:db (assoc db :leaderboard leaderboard)
    :dispatch [:active-page :end-game]
    }))

(re-frame/reg-event-fx
 :game/end
 (fn [{:keys [db]}]
   {:http-xhrio {:method :get
                 :uri (str locations/api "/games/" (get-in db [:server-state :id]) "/leaderboard")
                 :timeout 2000
                 :with-credentials true
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:game/get-leaderboard-success]
                 :on-failure [:request-failure]}
;;     :dispatch-later [{:ms 5000
;;                       :dispatch [:game/end] } ]
    }))

