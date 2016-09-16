(ns trivia.events
  (:require [re-frame.core :as re-frame]
            [trivia.db :as db]))

(re-frame/reg-event-fx
 :create-game
 (fn [cofx [_ data]]
   {:db (assoc (:db cofx) :current-question {:question "How cool is ClojureScript?"
                                             :answers [{:id 1 :answer "Meh" :correct false}
                                                       {:id 2 :answer "It's ok" :correct false}
                                                       {:id 3 :answer "(awesome \"it is\")" :correct true}
                                                       {:id 4 :answer "Rubbish" :correct false}]})
    :dispatch [:active-page :ask-question]}))

(re-frame/reg-event-fx
 :provide-answer
 [re-frame/debug]
 (fn [cofx [_ a]]
   ))

(re-frame/reg-event-db
 :active-page
 (fn [db [event data]]
   (assoc db :active-page data)))

(re-frame/reg-event-fx
 :login
 (fn [cofx [_ data]]
   {:db (assoc (:db cofx) :name data)
    :dispatch [:login-success]}))

(re-frame/reg-event-fx
 :login-success
 (fn [cofx]
   {:dispatch [:active-page :create-game]}))

(re-frame/reg-event-db
 :name
 (fn [db [event data]]
   (assoc db :name data))
 )

(re-frame/reg-event-db
 :initialise-db
 (fn [db]
   (console.log "Initialising!")
   db/default-value))
