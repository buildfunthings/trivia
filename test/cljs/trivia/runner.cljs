(ns trivia.runner
  (:require  [clojure.test :as t]
             [doo.runner :refer-macros [doo-tests]]))


(doo-tests 'trivia.core-test)
