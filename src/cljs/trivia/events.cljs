(ns trivia.events
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx :as http-fx]
            [re-frame.core :as re-frame]
            [trivia.db :as db]
            [trivia.locations :as locations]))

(defn reset-game [db]
  (-> db
      (assoc-in [:state :round] 1)
      (assoc-in [:state :correct] 0)
      (assoc-in [:state :incorrect] 0)
      (assoc :answer-state :unknown)))

(re-frame/reg-event-fx
 :create-game
 (fn [cofx]
   (let [db (:db cofx)
         q (rand-nth (:questions db))]
     {:db (-> db
              (reset-game)
              (assoc :current-question q))
      :dispatch [:active-page :ask-question]})))

(comment
  (fn [cofx [event question-id answer-id]]
   (let [db (:db cofx)
         q (get-in cofx [:db :current-question])
         answer (first (filter #(= (:id %) answer-id) (:answers q)))
         state (:state db)
         correct? (:correct answer)
         ]
     {:db  (-> db
               (assoc :answer-state (condp = (:correct answer)
                                         true :correct
                                         false :incorrect
                                         :unknown))
               (assoc-in [:state (if correct? :correct :incorrect)]
                         (inc (get state (if correct? :correct :incorrect)))))
      :dispatch-later [(if (= (:round state) (:max-rounds state))
                         {:ms 2000 :dispatch [:active-page :end-game]}
                         {:ms 2000 :dispatch [:next-question]})]}
     )))

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
                         {:ms 2000 :dispatch [:active-page :end-game]}
                         {:ms 2000 :dispatch [:next-question]})]})))

(re-frame/reg-event-db
 :answer-failure
 (fn [db [_ result]]
   (prn "Request failed: " result)
   db))

(re-frame/reg-event-fx
 :submit-answer
 (fn [cofx [event question-id answer-id]]
   {:http-xhrio {:method :post
                 :uri (str locations/api "/question/" question-id)
                 :params answer-id
                 :timeout 2000
                 :format (ajax/json-request-format)
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

(re-frame/reg-event-fx
 :next-question
 (fn [cofx]
   {:http-xhrio {:method :get
                 :uri (str locations/api "/question")
                 :timeout 2000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:question-success]
                 :on-failure [:question-failure]}
    }
   ))

(re-frame/reg-event-db
 :active-page
 (fn [db [event data]]
   (assoc db :active-page data)))

(re-frame/reg-event-fx
 :login
 (fn [cofx [event data]]
   {:db (assoc (:db cofx) :name data)
    :dispatch [:login-success]}))

(re-frame/reg-event-fx
 :login-success
 (fn [cofx]
   {:dispatch [:active-page :create-game]}))

(re-frame/reg-event-db
 :name
 (fn [db [event data]]
   (prn db)
   (assoc db :name data))
 )

(re-frame/reg-event-db
 :initialise-db
 (fn [db]
   (console.log "Initialising!")
   db/default-value))
