(ns app.core
  (:require [aide.core :as aide]
            [aide.lifetime :as aide-lifetime]))

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

(defn main
  []
  (let [; App
        *model (atom {:val 0})
        app (aide/object {:*model *model})

        ; UI
        value-el (.getElementById js/document "value")
        render (fn [model] (set! (.-innerHTML value-el) (str "#" (:val model))))]
    ; Connect UI to model
    (add-watch *model
               :render-watch
               (fn [_key _ref old-state new-state]
                 (when (not= old-state new-state)
                   (render new-state))))

    ; First render
    (render @*model)

    ; Start the app (in this example it does nothing)
    (aide/emit app aide-lifetime/on-start)

    ; Emit app events on DOM events
    (.addEventListener (.getElementById js/document "increment") "click" #(aide/emit app on-increment))
    (.addEventListener (.getElementById js/document "decrement") "click" #(aide/emit app on-decrement))
    (.addEventListener (.getElementById js/document "increment-if-odd") "click" #(aide/emit app on-increment-if-odd))
    (.addEventListener (.getElementById js/document "delayed-increment") "click" #(aide/emit app on-delayed-increment 1000))

    app))

(def app (main))

(defn figwheel-before-jsload
  []
  ; Stop the app (in this example it does nothing)
  (aide/emit app aide-lifetime/on-stop))