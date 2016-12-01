(ns trivia.views.end-game
  (:require [re-frame.core :as re-frame :refer [dispatch]]))

(defn list-player [{:keys [id username correct]}]
  ^{:key id}
  [:tr
   [:th.row id]
   [:td username]
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
              [:th "Correct"]]]
            [:tbody
             (doall (list-players @leaderboard))
             ]]]]
         [:div.row
          [:p
           [:a {:class "btn btn-primary btn-lg", :href "#", :role "button"
                :on-click #(dispatch [:create-game])}
            "Create a new game »"]]]]]])))
