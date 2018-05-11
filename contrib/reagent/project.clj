(defproject
  aide-reagent "0.1.0"
  :description "Reagent bindings for Aide."
  :url "https://github.com/metametadata/aide/tree/master/contrib/reagent"
  :license {:name "MIT" :url "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [org.clojure/clojurescript "1.10.238" :scope "provided"]

                 [reagent "0.7.0" :scope "provided"]

                 [aide "0.1.0"]]

  :pedantic? :abort

  :repositories {"clojars" {:sign-releases false}})