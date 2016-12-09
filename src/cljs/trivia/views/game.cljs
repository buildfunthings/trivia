(ns trivia.views.game
  (:require [re-frame.core :as re-frame]))

(defn create-answer [question-id answer]
  ^{:key (:id answer)}
  [:a {:class "btn btn-lg btn-default btn-block", :href "#", :role "button"
       :on-click #(re-frame/dispatch [:game/submit-answer question-id (:id answer)])} (:answer answer)])

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
