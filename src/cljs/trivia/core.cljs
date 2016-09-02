(ns trivia.core
  (:require [reagent.core :as reagent]))

(defn add-numbers [a b]
  (+ a b))

(defn mount-root []
  (reagent/render [:h1 "Hello World!"]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (mount-root))

