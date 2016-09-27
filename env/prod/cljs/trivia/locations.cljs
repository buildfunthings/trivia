(ns trivia.locations)

;;
;; In the production environment, assume we are running the API on the same
;; webserver
;;
(def api (str (.-protocol js/location) "//" (.-hostname js/location) ":" (.-port js/location) "/api"))
