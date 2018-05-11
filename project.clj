(defproject
  aide "0.1.0"
  :description "ClojureScript/Clojure event-driven application framework."
  :url "https://github.com/metametadata/aide"
  :license {:name "MIT" :url "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [org.clojure/clojurescript "1.10.238" :scope "provided"]]

  :pedantic? :abort

  :plugins [[com.jakemccrary/lein-test-refresh "0.21.1"]
            [lein-doo "0.1.9" :exclusions [org.clojure/clojure]]]

  :source-paths ["src" "test"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "resources/private" "target"]

  :test-refresh {:notify-command ["terminal-notifier" "-title" "Tests" "-message"]
                 :quiet          true}

  :cljsbuild {:builds [{:id           "test"
                        :source-paths ["test" "src"]
                        :compiler     {:main          unit.runner
                                       :output-to     "resources/private/js/compiled/testable.js"
                                       :output-dir    "resources/private/js/compiled/out"
                                       :optimizations :none}}]}

  :repositories {"clojars" {:sign-releases false}})