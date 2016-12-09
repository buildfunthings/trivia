(ns trivia.views.login
  (:require [re-frame.core :as re-frame :refer [dispatch]]
            [reagent.core :as reagent]))

(defn login-panel []
  (let [name (reagent/atom "")
        password (reagent/atom "")]
    (fn []
      [:div {:class "container"}
       [:div {:class "row"}
        [:div {:class "col-md-4 col-md-offset-4"}
         [:h3 "Please Log In"]
         [:form {:role "form"}
          [:div {:class "form-group"}
           [:label {:for "inputUsernameEmail"} "Username"]
           [:input {:type "text", :class "form-control", :id "inputUsername",
                    :on-change #(reset! name (-> % .-target .-value))}]]
          [:div {:class "form-group"}
           [:label {:for "inputPassword"} "Password"]
           [:input {:type "password", :class "form-control", :id "inputPassword"
                    :on-change #(reset! password (-> % .-target .-value))}]]
          [:div {:class "btn btn btn-primary"
                 :on-click #(dispatch [:login/post {:username @name :password @password}])} "Log In"]
          ]]]
       [:div {:class "row"}
        [:div {:class "col-md-4 col-md-offset-4"}
         "No account? "
         [:a {:on-click #(dispatch [:signup/navigate])} "Create one now!"]]]])))
