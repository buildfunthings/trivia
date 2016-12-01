(ns trivia.events.game
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :as re-frame]
            [trivia.locations :as locations]))

(re-frame/reg-event-fx
 :game/next-question
 (fn [{:keys [db]}]
   (let [cq (first (:questions db))
         rq (rest (:questions db))
         round (get-in db [:state :round])]
     
     {:db (-> db
              (assoc-in [:state :round] (inc round))
              (assoc :answer-state :unknown)
              (assoc :current-question cq)
              (assoc :questions rq))})
   ))

(re-frame/reg-event-fx
 :game/answer-verified
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
                         {:ms 2000 :dispatch [:game/end]}
                         {:ms 2000 :dispatch [:game/next-question]})]})))

;; (re-frame/reg-event-db
;;  :answer-failure
;;  (fn [db [_ result]]
;;    (prn "Request failed: " result)
;;    db))

(re-frame/reg-event-fx
 :game/submit-answer
 (fn [{:keys [db]} [event question-id answer-id]]
   {:http-xhrio {:method :post
                 :uri (str locations/api "/games/" (get-in db [:server-state :id]) "/verify/" question-id)
                 :params answer-id
                 :timeout 2000
                 :format (ajax/json-request-format)
                 :with-credentials true
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:game/answer-verified]
                 :on-failure [:request-failure]}}))

;; (re-frame/reg-event-db
;;  :question-success
;;  (fn [db [_ result]]
;;    (let [round (get-in db [:state :round])]
;;      (-> db
;;          (assoc-in [:state :round] (inc round))
;;          (assoc :answer-state :unknown)
;;          (assoc :current-question result)))))
;; 
;; (re-frame/reg-event-db
;;  :question-failure
;;  (fn [db [_ result]]
;;    (prn "Request failed: " result)
;;    db))


