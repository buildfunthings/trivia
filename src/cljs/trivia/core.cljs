(ns trivia.core
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [trivia.events :as events]
            [trivia.subs :as subs]
            [trivia.views :as views]))

(enable-console-print!)

(defn add-numbers [a b]
  (+ a b))

(defn mount-root []
  (reagent/render [views/main-page]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialise-db])
  (mount-root))

