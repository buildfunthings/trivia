(defproject trivia "0.0.12"
  :description "Trivia Game"
  :url "https://www.buildfunthings.com"

  :license {:name "GPLv3"
            :url "http://choosealicense.com/licenses/gpl-3.0/#"}

  :dependencies [[clojure.jdbc/clojure.jdbc-c3p0 "0.3.2"]
                 [com.fzakaria/slf4j-timbre "0.3.2"]
                 [com.layerware/hugsql "0.4.7"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.taoensso/timbre "4.7.4"]
                 [day8.re-frame/http-fx "0.1.2"]
                 [environ "1.1.0"]
                 [http-kit "2.2.0"]
                 [metosin/compojure-api "1.1.9"]
                 [migratus "0.8.32"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.postgresql/postgresql "9.4.1212"]
                 [prismatic/schema "1.1.3"]
                 [re-frame "0.8.0"]
                 [reagent "0.6.0"]
                 [ring-cors "0.1.8"]
                 [ring.middleware.logger "0.5.0"]
                 [ring/ring-mock "0.3.0"]
                 [buddy/buddy-core "1.1.1"]
                 [buddy/buddy-auth "1.2.0"]
                 [buddy/buddy-hashers "1.0.0"]
                 [reagent-forms "0.5.28"]]

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
                   {:dependencies [[binaryage/devtools "0.8.3"]
                                   [com.cemerick/piggieback "0.2.1"] 
                                   [figwheel-sidecar "0.5.8"]]
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
