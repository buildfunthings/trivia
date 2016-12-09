(ns trivia.views
  (:require [re-frame.core :as re-frame]
            trivia.views.create-game
            trivia.views.end-game
            trivia.views.game
            trivia.views.login
            trivia.views.signup))

(defmulti pages identity)

(defmethod pages :signup [] [(trivia.views.signup/user-signup)])
(defmethod pages :login [] [(trivia.views.login/login-panel)])
(defmethod pages :create-game [] [(trivia.views.create-game/create-game)])
(defmethod pages :ask-question [] [(trivia.views.game/ask-question)])
(defmethod pages :end-game [] [(trivia.views.end-game/end-game)])

(defn show-page
  [page-name]
  [pages page-name])

(defn main-page []
  (let [active-page (re-frame/subscribe [:active-page])]
    (fn []
      [:div
       (show-page @active-page)
       ])))
