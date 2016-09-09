(ns trivia.events
  (:require [re-frame.core :as re-frame]
            [trivia.db :as db]))

(re-frame/reg-event-fx
 :create-game
 (fn [db]
   {:db (assoc db :current-question {:question "How cool is ClojureScript?"
                                     :answers [{:answer "Meh" :correct false}
                                               {:answer "It's ok" :correct false}
                                               {:answer "(awesome \"it is\")" :correct true}
                                               {:answer "Rubbish" :correct false}]})
    :dispatch [:active-page :ask-question]}))

(re-frame/reg-event-db
 :active-page
 (fn [db [event data]]
   (assoc db :active-page data)))

(re-frame/reg-event-fx
 :login
 (fn [db [event data]]
   {:db (assoc db :name data)
    :dispatch [:login-success]}))

(re-frame/reg-event-fx
 :login-success
 (fn [db]
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
