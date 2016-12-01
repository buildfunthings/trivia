(ns trivia.views.create-game
  (:require [re-frame.core :as re-frame :refer [dispatch]]))


;; (defn create-player-form-entry [{:keys [id username]}]
;;   ^{:key id} [:li.list-group-item {:key id} username])

;; (defn select-player-form [friends]
;;   [:ul.list-group {:field :multi-select :id :players}
;;    (map #(create-player-form-entry %) friends)
;;    ])

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
  (prn friends)
  (let [s (filter #(= true (:selected? %)) friends)]
    (map #(:id %) s)))

(defn create-game []
  (let [friends (re-frame/subscribe [:friends])]
    (fn []
       [:div {:class "container"}
            [:div {:class "row"}
             [:div {:class "jumbotron"}
              [:div {:class "container"}
               [:h1 "Trivia Game!"]
               [:p "The most exciting game since solitaire."]]]
             (select-player-form-2 @friends)
             [:p
              [:a {:class "btn btn-primary btn-lg", :href "#", :role "button"
                   :on-click #(dispatch [:game/create (get-player-ids @friends)])}
               "Create a new game »"]]
             ]]

      )))
