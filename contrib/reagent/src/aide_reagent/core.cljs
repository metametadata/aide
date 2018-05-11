(ns aide-reagent.core
  (:require [aide.core :as aide]
            [reagent.core :as r]
            [reagent.ratom :refer [reaction]]))

(defn atom->reaction
  "Creates a reaction which syncs its value with the specified atom."
  [*a]
  (let [*ra (r/atom @*a)]
    (add-watch *a
               *ra                                          ; Unique key
               (fn [_key _ref _old-state new-state]
                 (reset! *ra new-state)))
    (reaction @*ra)))

(defn connect
  "Arguments:

  * `view` - Reagent component function with args: `[view-model emit]` (`emit` signature is `[event data]`, `data` is optional)
  * `view-model` - will be passed into view
  * `app` - the object that will handle emitted events

  Returns a Reagent component that has no args."
  [view view-model app]
  (letfn [(view-emit [& args] (apply aide/emit app args))]
    [view view-model view-emit]))