(defproject
  aide-persistence "0.2.0"
  :description "Naive Aide middleware for automatic model saving/loading using browser storage."
  :url "https://github.com/metametadata/aide/tree/master/contrib/persistence"
  :license {:name "MIT" :url "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [org.clojure/clojurescript "1.10.238" :scope "provided"]

                 [aide "0.1.0"]]

  :pedantic? :abort

  :repositories {"clojars" {:sign-releases false}})