(ns trivia.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :state
 (fn [db _]
   (:state db)))

(re-frame/reg-sub
 :leaderboard
 (fn [db _]
   (:leaderboard db)))

(re-frame/reg-sub
 :friends
 (fn [db _]
   (:friends db)))

(re-frame/reg-sub
 :open-games
 (fn [db _]
   (:open-games db)))

(re-frame/reg-sub
 :server-state
 (fn [db _]
   (:server-state db)))

(re-frame/reg-sub
 :current-question
 (fn [db _]
   (:current-question db)))

(re-frame/reg-sub
 :answer-state
 (fn [db _]
   (:answer-state db)))

(re-frame/reg-sub
 :name
 (fn [db _]
   (:name db)))

(re-frame/reg-sub
 :active-page
 (fn [db _]
   (:active-page db)))
