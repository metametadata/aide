(ns app.view-model
  (:require [reagent.ratom :refer [reaction]]))

(defn view-model
  [*model]
  {:*query   (reaction (:query @*model))
   :*friends (reaction (:friends @*model))})