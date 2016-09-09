(ns trivia.subs
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
 :current-question
 (fn [db _]
   (:current-question db)))

(re-frame/reg-sub
 :name
 (fn [db _]
   (:name db)))

(re-frame/reg-sub
 :active-page
 (fn [db _]
   (:active-page db)))
