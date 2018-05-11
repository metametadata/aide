(ns app.core
  (:require [aide.core :as aide]
            [aide.lifetime :as aide-lifetime]
            [aide-reagent.core :as aide-reagent]
            [reagent.core :as r]
            [reagent.ratom :refer [reaction]]))

(enable-console-print!)

(aide/defevent on-increment
  [app _]
  (swap! (:*model app) update :val inc))

(aide/defevent on-decrement
  [app _]
  (swap! (:*model app) update :val dec))

(aide/defevent on-increment-if-odd
  [app _]
  (when (odd? (:val @(:*model app)))
    (swap! (:*model app) update :val inc)))

(aide/defevent on-delayed-increment
  [app delay-ms]
  (.setTimeout js/window #(aide/emit app on-increment) delay-ms))

(defn view-model
  [*model]
  {:*counter (reaction (str "#" (:val @*model)))})

(defn view
  [view-model emit]
  [:p
   @(:*counter view-model) " "
   [:button {:on-click #(emit on-increment)} "+"] " "
   [:button {:on-click #(emit on-decrement)} "-"] " "
   [:button {:on-click #(emit on-increment-if-odd)} "Increment if odd"] " "
   [:button {:on-click #(emit on-delayed-increment 1000)} "Delayed increment"]])

(defn main
  []
  (let [; App
        *model (atom {:val 0})
        app (aide/object {:*model *model})

        ; UI
        view-model (view-model (aide-reagent/atom->reaction *model))]
    (-> view
        (aide-reagent/connect view-model app)
        (r/render (.getElementById js/document "root")))

    ; Start the app (in this example it does nothing)
    (aide/emit app aide-lifetime/on-start)

    ; For easier debugging
    (assoc app :view-model view-model)))

(def app (main))

(defn figwheel-before-jsload
  []
  ; Stop the app (in this example it does nothing)
  (aide/emit app aide-lifetime/on-stop))