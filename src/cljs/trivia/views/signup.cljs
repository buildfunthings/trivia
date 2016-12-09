(ns trivia.views.signup
  (:require [re-frame.core :as re-frame :refer [dispatch]]
            [reagent.core :as reagent]))

(defn are-these-the-same? [this that]
  (= this that))

(defn all-signup-fields-done? [username password confirm]
  (and (not (empty? username))
       (= password confirm)))

(defn user-signup []
  (let [username (reagent/atom "")
        password (reagent/atom "")
        pwconfirm (reagent/atom "")]
    (fn []
      [:div.col-md-6
       [:div#logbox
        [:form {:role "form"}
         [:h1 "Create an Account"]
         [:div {:class "form-group"}
          [:label {:for "inputUsername"} "Username"]
          [:input {:type "text", :class "form-control", :id "inputUsername",
                   :on-change #(reset! username (-> % .-target .-value))
                   }]]
         [:div {:class "form-group"}
          [:label {:for "inputPassword"} "Password"]
          [:input {:type "password", :class "form-control", :id "inputPassword",
                   :on-change #(reset! password (-> % .-target .-value))
                   }]]
         [:div {:class (str "form-group" (when (not (are-these-the-same? @password @pwconfirm)) " has-error"))}
          [:label {:for "inputConfirmPassword"} "Confirm Password"]
          [:input {:type "password", :class "form-control", :id "inputConfirmPassword",
                   :on-change #(reset! pwconfirm (-> % .-target .-value))
                   }]]

         [:div {:class "btn btn-primary" :disabled (not (all-signup-fields-done? @username
                                                                                 @password
                                                                                 @pwconfirm))
                :on-click #(dispatch [:signup/perform @username @password])
                } "Create Account"]
         ]]])))
