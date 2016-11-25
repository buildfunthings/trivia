(ns trivia.views
  (:require [re-frame.core :as re-frame :refer [dispatch subscribe]]
            [reagent-forms.core :as rf :refer [bind-fields]]
            [reagent.core :as reagent]
            [taoensso.timbre :as log]))

(defn navbar []
  (let [name (re-frame/subscribe [:name])]
    [:nav {:class "navbar navbar-inverse navbar-fixed-top", :role "navigation"}
     [:div {:class "container"}
      [:div {:class "navbar-header"}
       [:button {:type "button", :class "navbar-toggle collapsed", :data-toggle "collapse",
                 :data-target "#navbar", :aria-expanded "false", :aria-controls "navbar"}
        [:span {:class "sr-only"}
         "Toggle navigation"]
        [:span {:class "icon-bar"}]
        [:span {:class "icon-bar"}]
        [:span {:class "icon-bar"}]
        ]
       [:a {:class "navbar-brand", :href "#"}
        "Trivia Game"]
       ]
      [:div {:id "navbar", :class "navbar-collapse collapse"}
       [:div {:class "nav navbar-nav navbar-right"}
        [:ul {:class "nav navbar-nav"}
         [:li
          [:a {:href "#contact"}
           @name]
          ]]
        ]]]
     ]))

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
                 :on-click #(dispatch [:login {:username @name :password @password}])} "Log In"]
          ]]]
       [:div {:class "row"}
        [:div {:class "col-md-4 col-md-offset-4"}
         "No account? "
         [:a {:on-click #(dispatch [:dosignup])} "Create one now!"]]]
       [:div {:class "row"}
        [:div {:class "col-md-4 col-md-offset-4"}
         [:a {:on-click #(dispatch [:choose-opponents])} "Experiment!"]]]])))

(defn create-player-form-entry [{:keys [id username]}]
  ^{:key id} [:li.list-group-item {:key id} username])

(defn select-player-form [friends]
  (prn "Friends " (map #(:username %) friends))
  [:ul.list-group {:field :multi-select :id :players}
   (map #(create-player-form-entry %) friends)
   ])

(defn create-game []
  (let [doc (reagent/atom {:players []})
        friends (re-frame/subscribe [:friends])]
     (fn []
          [:div {:class "container"}
           ;;(navbar)
           [:div {:class "row"}
            [:div {:class "jumbotron"}
             [:div {:class "container"}
              [:h1 "Trivia Game!"]
              [:p "The most exciting game since solitaire."]]]

            [:h1 "Select an opponent"]
            [bind-fields (select-player-form @friends) doc]
            [:p
             [:a {:class "btn btn-primary btn-lg", :href "#", :role "button" :on-click #(dispatch [:create-game (:players @doc)])}
              "Create a new game »"]]
            [:p "Current document: " @doc]]])))

(defn create-answer [question-id answer]
  ^{:key (:id answer)}
  [:a {:class "btn btn-lg btn-default btn-block", :href "#", :role "button"
       :on-click #(re-frame/dispatch [:submit-answer question-id (:id answer)])} (:answer answer)])

(defn ask-question []
  (let [question (re-frame/subscribe [:current-question])
        answer-state (re-frame/subscribe [:answer-state])
        state (re-frame/subscribe [:state])
        server-state (re-frame/subscribe [:server-state])]
    (fn []
      [:div {:class "container"}
       [:div {:class "row"}
        [:div {:class "col-md-8 col-md-offset-2"}
         [:h1 "Question " (:round @state) " of " (:max-rounds @state) ]
         ]]
       [:div {:class "row"}
        [:div {:class "col-md-8 col-md-offset-2"}
         [:div {:class "jumbotron"}
          [:div {:class (str "container text-center " (condp = @answer-state
                                                        :correct "correct-answer"
                                                        :incorrect "incorrect-answer"
                                                        :unknown ""))}
           [:h2 (:question @question)]
           ]]
         ]]
       [:div {:class "row"}
        [:div {:class "col-md-8 col-md-offset-2"}
         (doall (map #(create-answer (:id @question) %) (:answers @question)))
         ]]
       [:div {:class "row"}
        [:div {:class "col-md-8 col-md-offset-2"}
         @state - @server-state
         ]]])))

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
                :on-click #(dispatch [:signup @username @password])
                } "Create Account"]
         ]]])))

(defn opponents []
  (fn []
    [:div.row
     [:div.col-md-8.col-md-offset-2
      [:p "Select oponents"]
      [:ul
       [:li "Rebecca"]
       [:li "Jelle"]
       [:li "Binky"]]
      [:p [:a.btn.btn-default {:href "#" :role "button"} "Create Game »"]]]]))

(defmulti pages identity)

(defmethod pages :signup [] [(user-signup)])
(defmethod pages :login [] [(login-panel)])
(defmethod pages :create-game [] [(create-game)])
(defmethod pages :choose-opponents [] [(opponents)])
(defmethod pages :ask-question [] [(ask-question)])
(defmethod pages :end-game [] [(end-game)])

(defn show-page
  [page-name]
  (prn "Page: " page-name)
  [pages page-name])

(defn main-page []
  (let [active-page (re-frame/subscribe [:active-page])]
    (fn []
      [:div
       (show-page @active-page)
       ])))
