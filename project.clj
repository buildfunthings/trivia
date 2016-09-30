(defproject trivia "0.0.1-SNAPSHOT"
  :description "Trivia Game"
  :url "https://www.buildfunthings.com"

  :license {:name "GPLv3"
            :url "http://choosealicense.com/licenses/gpl-3.0/#"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [environ "1.1.0"]
                 [reagent "0.6.0"]
                 [re-frame "0.8.0"]
                 [http-kit "2.1.18"]
                 [com.stuartsierra/component "0.3.1"]
                 [metosin/compojure-api "1.1.8"]
                 [prismatic/schema "1.1.3"]
                 [day8.re-frame/http-fx "0.0.4"]
                 [ring-cors "0.1.8"]
                 [ring.middleware.logger "0.5.0"]
                 [com.layerware/hugsql "0.4.7"]
                 [migratus "0.8.32"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.2"]
                 [com.taoensso/timbre "4.7.4"]
                 [com.fzakaria/slf4j-timbre "0.3.2"]
                 [org.postgresql/postgresql "9.4.1211"]
                 ]

  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-environ "1.1.0"]]

  :min-lein-version "2.6.1"

  :main ^:skip-aot trivia.core
  :uberjar-name "trivia.jar"
  
  :target-path "target/%s"

  :source-paths ["src/clj" "src/cljs"]
  :test-paths ["test/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"
                                    "test/js"]

  :profiles {:uberjar {:aot :all
                       :omit-source true
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       }
             :dev [:dev-overrides
                   {:dependencies [[figwheel-sidecar "0.5.7"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [binaryage/devtools "0.8.1"]]
                   :plugins [[lein-figwheel "0.5.7"]
                             [lein-doo "0.1.7"]]}]}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs" "env/dev/cljs"]
                        :figwheel {:on-jsload "trivia.core/mount-root"}
                        :compiler {:main trivia.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :source-map-timestamp true}}
                       {:id           "min"
                        :source-paths ["src/cljs" "env/prod/cljs"]
                        :compiler     {:main            trivia.core
                                       :output-to       "resources/public/js/compiled/app.js"
                                       :output-dir      "resources/public/js/compiled/out-min"
                                       :optimizations   :advanced
                                       :closure-defines {goog.DEBUG false}
                                       :pretty-print    false}}
                       {:id "test"
                        :source-paths ["src/cljs" "test/cljs" "env/prod/cljs"]
                        :compiler {:output-to "resources/public/js/compiled/test.js"
                                   :main trivia.runner
                                   :optimizations :none}}]}
  )
