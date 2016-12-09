(ns trivia.events
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :as re-frame]
            [trivia.db :as db]
            [trivia.locations :as locations]
            [trivia.events.login]
            [trivia.events.create-game]
            [trivia.events.game]
            [trivia.events.end-game]
            [trivia.events.signup]))

(re-frame/reg-event-db
 :request-failure
 (fn [db [_ result]]
   (prn "Request failed: " result)
   db))

(re-frame/reg-event-db
 :active-page
 (fn [db [event data]]
   (assoc db :active-page data)))

(re-frame/reg-event-db
 :name
 (fn [db [event data]]
   (assoc db :name data))
 )

(re-frame/reg-event-db
 :initialise-db
 (fn [cofx]
   (console.log "Initialising!")
   db/default-value))
