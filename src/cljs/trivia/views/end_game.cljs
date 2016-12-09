(ns trivia.views.end-game
  (:require [re-frame.core :as re-frame :refer [dispatch]]))

(defn list-player [{:keys [id username answered correct]}]
  ^{:key id}
  [:tr
   [:th.row id]
   [:td username]
   [:td answered]
   [:td correct]])

(defn list-players [leaderboard]
  (map #(list-player %) leaderboard))

(defn end-game []
  (let [state (re-frame/subscribe [:state])
       leaderboard (re-frame/subscribe [:leaderboard])]
    (fn []
      [:div.container
       [:div.row
        [:div.col-md-6.col-md-offset-3
         [:h1 "Leaderboard"]
         [:div.row
          [:div
           [:table.table
            [:thead
             [:tr
              [:th "#"]
              [:th "Username"]
              [:th "Answered"]
              [:th "Correct"]]]
            [:tbody
             (doall (list-players @leaderboard))
             ]]]]
         [:div.row
          [:p
           [:a {:class "btn btn-primary btn-lg", :href "#", :role "button"
                :on-click #(dispatch [:login/success])}
            "Create a new game »"]]]]]])))
