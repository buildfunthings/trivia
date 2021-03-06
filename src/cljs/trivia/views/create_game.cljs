(ns trivia.views.create-game
  (:require [re-frame.core :as re-frame :refer [dispatch]]))

(defn- create-checklist-item [{:keys [id username selected?]}]
  ^{:key id} [:li.list-group-item {:on-click  #(re-frame/dispatch [:game/select-player id])
                                   :class (when selected? "active")
                                   :data-checked selected?}
                 username])

(defn select-player-form-2 [friends]
  [:div.row
   [:div.col-xs-12
    [:h3.text-center "Select opponent"]
    [:div.well
     [:ul.list-group.checked-list-box
      (map create-checklist-item friends)
      ]]]])

(defn- get-player-ids [friends]
  (let [s (filter #(= true (:selected? %)) friends)]
    (map #(:id %) s)))

(defn- create-player-entry [{:keys [user_id correct answered]}]
  ^{:key user_id}
  [:li.list-group-item "Player " [:b user_id] " currently answered " answered])

(defn- create-game-entry [[game-id players] user]
  (let [me (filter #(= (:id user) (:user_id %)) players)]
    ^{:key game-id} [:li.list-group-item {:on-click  #(re-frame/dispatch [:game/resume game-id me])
                                          ;;:class (when selected? "active")
                                          ;;:data-checked selected?
                                          }
                     [:h4 "Game " game-id " waiting for "]
                     [:ul.list-group.checked-list-box
                      (map create-player-entry players)]
                     ]))

(defn prev-games-list [prev-games user]
  [:div.row
   [:div.col-xs-12
    [:h3.text-center "Previous games"]
    [:div.well
     [:ul.list-group.checked-list-box
      (map #(create-game-entry % user) (reverse prev-games))
      ]]]])

(defn create-game []
  (let [friends (re-frame/subscribe [:friends])
        prev-games (re-frame/subscribe [:prev-games])
        user (re-frame/subscribe [:user])]
    (fn []
       [:div {:class "container"}
            [:div {:class "row"}
             [:div {:class "jumbotron"}
              [:div {:class "container"}
               [:h1 "Trivia Game!"]
               [:p "The most exciting game since solitaire."]]]
             [:div.row
              [:div.col-sm-6 
               (prev-games-list @prev-games @user)]
              [:div.col-sm-6
               (select-player-form-2 @friends)
               [:p
                [:a {:class "btn btn-primary btn-lg", :href "#", :role "button"
                     :on-click #(dispatch [:game/create (get-player-ids @friends)])}
                 "Create a new game »"]]]]
             ]]

      )))
