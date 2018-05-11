(defproject
  aide-history "0.3.0"
  :description "Aide middleware which simplifies working with browser history."
  :url "https://github.com/metametadata/aide/tree/master/contrib/history"
  :license {:name "MIT" :url "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [org.clojure/clojurescript "1.10.238" :scope "provided"]

                 [aide "0.1.0"]]

  :pedantic? :abort

  :repositories {"clojars" {:sign-releases false}})