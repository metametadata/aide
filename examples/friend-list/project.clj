(defproject
  friend-list "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]

                 [reagent "0.7.0" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [cljsjs/react "15.6.2-1"]
                 [cljsjs/react-dom "15.6.2-1"]

                 [prismatic/schema "1.1.7"]

                 [funcool/hodgepodge "0.1.4"]]

  :profiles {:dev {:dependencies [; Chrome DevTools enhancements
                                  [binaryage/devtools "0.9.4"]]}}

  :pedantic? :abort

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.15" :exclusions [org.clojure/clojure]]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "resources/private" "target"]

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src"
                                       "../../src"
                                       "../../contrib/reagent/src"
                                       "../../contrib/history/src"
                                       "../../contrib/logging/src"
                                       "../../contrib/persistence/src"]
                        :compiler     {:main                 app.core
                                       :asset-path           "js/compiled/out"
                                       :output-to            "resources/public/js/compiled/frontend.js"
                                       :output-dir           "resources/public/js/compiled/out"
                                       :source-map-timestamp true
                                       :parallel-build       true
                                       :compiler-stats       true
                                       :preloads             [devtools.preload]}
                        :figwheel     {:before-jsload "app.core/figwheel-before-jsload"}}

                       {:id           "min"
                        :source-paths ["src"
                                       "../../src"
                                       "../../contrib/reagent/src"
                                       "../../contrib/history/src"
                                       "../../contrib/logging/src"
                                       "../../contrib/persistence/src"]
                        :compiler     {:main           app.core
                                       :output-to      "resources/public/js/compiled/frontend.js"
                                       :optimizations  :advanced
                                       :pretty-print   false
                                       :compiler-stats true
                                       :parallel-build false}}]})
