(ns trivia.events
  (:require [re-frame.core :as re-frame]
            [trivia.db :as db]))


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

(re-frame/reg-event-fx
 :submit-answer
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


(re-frame/reg-event-db
 :next-question
 (fn [db]
   (let [q (rand-nth (:questions db))
         round (get-in db [:state :round])]
     (-> db
         (assoc-in [:state :round] (inc round))
         (assoc :answer-state :unknown)
         (assoc :current-question q)))
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
