{:dev-overrides  {:env {:app-version :project/version
                        :database-url "postgres://postgres:password1@development:5432/trivia"
                        :cors "http://development:3449,http://development:8080"
                        :host "development"
                        :port "8080"
                        :timbre_ns_blacklist "com.mchange"
                        }}}


