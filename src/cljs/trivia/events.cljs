(ns trivia.events
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx :as http-fx]
            [re-frame.core :as re-frame]
            [trivia.db :as db]
            [trivia.locations :as locations]))

(defn reset-game [db]
  (-> db
      (assoc-in [:state :round] 0)
      (assoc-in [:state :correct] 0)
      (assoc-in [:state :incorrect] 0)
      (assoc :answer-state :unknown)))

(re-frame/reg-event-db
 :select-player
 (fn [db [_ player]]
   (let [friends (:friends db)]
     (assoc db :friends 
            (map #(if (= player (:id %))
                    (assoc % :selected? (not (:selected? %)))
                    %)
                 friends)))))

(re-frame/reg-event-fx
 :dosignup
 (fn [cofx]
   {:dispatch [:active-page :signup]}))

(re-frame/reg-event-fx
 :choose-opponents
 (fn [cofx]
   {:dispatch [:active-page :choose-opponents]}))

(re-frame/reg-event-fx
 :get-questions-success
 (fn [{:keys [db]} [_ questions]]
   {:db (assoc db :questions questions)
    :dispatch-n (list [:next-question] [:active-page :ask-question])
    }))

(re-frame/reg-event-fx
 :create-game-success
 (fn [{:keys [db]} [_ game]]
   {:db (assoc db :server-state game)
    :http-xhrio {:method :get
                 :uri (str locations/api "/games/" (:id game) "/questions")
                 :timeout 2000
                 :with-credentials true
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:get-questions-success]
                 :on-failure [:request-failure]}}))

(re-frame/reg-event-fx
 :get-leaderboard-success
 (fn [{:keys [db]} [_ leaderboard]]
   (prn leaderboard)
   {:db (assoc db :leaderboard leaderboard)
    :dispatch [:active-page :end-game]
    }))

(re-frame/reg-event-fx
 :end-game
 (fn [{:keys [db]}]
   {:http-xhrio {:method :get
                 :uri (str locations/api "/games/" (get-in db [:server-state :id]) "/leaderboard")
                 :timeout 2000
                 :with-credentials true
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:get-leaderboard-success]
                 :on-failure [:request-failure]}
    }))

(re-frame/reg-event-fx
 :friend-list-success
 (fn [{:keys [db]} [_ friends]]
   {:db (assoc db :friends friends)}))

(re-frame/reg-event-fx
 :get-friend-list
 (fn [{:keys [db]}]
   {:http-xhrio {:method :get
                 :uri (str locations/api "/users")
                 :timeout 2000
                 :with-credentials true
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:friend-list-success]
                 :on-failure [:request-failure]}
    }))

(re-frame/reg-event-fx
 :create-game
 (fn [{:keys [db]} [_ players]]
   {:db (-> db (reset-game))
    :http-xhrio {:method :post
                 :uri (str locations/api "/games")
                 :params {:players players}
                 :timeout 2000
                 :format (ajax/json-request-format)
                 :with-credentials true
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:create-game-success]
                 :on-failure [:request-failure]}
    }))

(re-frame/reg-event-fx
 :answer-success
 (fn [cofx [event result]]
   (let [db (:db cofx)
         state (:state db)
         correct? (:correct? result)]
     {:db  (-> db
               (assoc :answer-state (condp = correct?
                                      true :correct
                                      false :incorrect
                                      :unknown))
               (assoc-in [:state (if correct? :correct :incorrect)]
                         (inc (get state (if correct? :correct :incorrect)))))
      :dispatch-later [(if (= (:round state) (:max-rounds state))
                         {:ms 2000 :dispatch [:end-game]}
                         {:ms 2000 :dispatch [:next-question]})]})))

(re-frame/reg-event-db
 :answer-failure
 (fn [db [_ result]]
   (prn "Request failed: " result)
   db))

(re-frame/reg-event-fx
 :signup
 (fn [cofx [event username password]]
   {:http-xhrio {:method :post
                 :uri (str locations/api "/signup")
                 :params {:username username
                          :password password}
                 :timeout 2000
                 :format (ajax/json-request-format)
                 :with-credentials true
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:login-success]
                 :on-failure [:request-failure]}}))

(re-frame/reg-event-fx
 :submit-answer
 (fn [{:keys [db]} [event question-id answer-id]]
   {:http-xhrio {:method :post
                 :uri (str locations/api "/games/" (get-in db [:server-state :id]) "/verify/" question-id)
                 :params answer-id
                 :timeout 2000
                 :format (ajax/json-request-format)
                 :with-credentials true
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:answer-success]
                 :on-failure [:answer-failure]}}))

(re-frame/reg-event-db
 :question-success
 (fn [db [_ result]]
   (let [round (get-in db [:state :round])]
     (-> db
         (assoc-in [:state :round] (inc round))
         (assoc :answer-state :unknown)
         (assoc :current-question result)))))

(re-frame/reg-event-db
 :question-failure
 (fn [db [_ result]]
   (prn "Request failed: " result)
   db))

(re-frame/reg-event-db
 :request-failure
 (fn [db [_ result]]
   (prn "Request failed: " result)
   db))

(re-frame/reg-event-fx
 :next-question
 (fn [{:keys [db]}]
   (let [cq (first (:questions db))
         rq (rest (:questions db))
         round (get-in db [:state :round])]
     
     {:db (-> db
              (assoc-in [:state :round] (inc round))
              (assoc :answer-state :unknown)
              (assoc :current-question cq)
              (assoc :questions rq))})
   ;; {:http-xhrio {:method :get
   ;;               :uri (str locations/api "/question")
   ;;               :with-credentials true
   ;;               :timeout 2000
   ;;               :response-format (ajax/json-response-format {:keywords? true})
   ;;               :on-success [:question-success]
   ;;               :on-failure [:question-failure]}
   ;;  }
   ))

(re-frame/reg-event-db
 :active-page
 (fn [db [event data]]
   (assoc db :active-page data)))

(re-frame/reg-event-fx
 :login
 (fn [cofx [event data]]
   {:db (assoc (:db cofx) :name data)
    :http-xhrio {:method :post
                 :uri (str locations/api "/login")
                 :with-credentials true
                 :params data
                 :format (ajax/json-request-format)
                 :timeout 2000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:login-success]
                 :on-failure [:request-failure]}
    ;;:dispatch [:login-success]
    }))

(re-frame/reg-event-fx
 :login-success
 (fn [cofx]
   {:dispatch-n (list
                 [:get-friend-list]
                 [:active-page :create-game]
                 )}))

(re-frame/reg-event-db
 :name
 (fn [db [event data]]
   (prn db)
   (assoc db :name data))
 )

(re-frame/reg-event-db
 :initialise-db
 (fn [cofx]
   (console.log "Initialising!")
   db/default-value))
