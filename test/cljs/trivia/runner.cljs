(ns trivia.runner
  (:require  [clojure.test :as t]
             [doo.runner :refer-macros [doo-tests]]
             [trivia.core-test]))

(enable-console-print!)

(doo-tests 'trivia.core-test)
