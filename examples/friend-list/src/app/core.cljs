(ns app.core
  (:require [app.model :as model]
            [app.events :as events]
            [app.view-model :as view-model]
            [app.view :as view]
            [aide.core :as aide]
            [aide.lifetime :as aide-lifetime]
            [aide-history.core :as aide-history]
            [aide-reagent.core :as aide-reagent]
            [aide-logging.core :as aide-logging]
            [aide-persistence.core :as aide-persistence]
            [schema.core :as s]
            [hodgepodge.core :as hp]
            [reagent.core :as r]
            [reagent.ratom :refer [reaction]]))

(enable-console-print!)

(def -not-persisted-model-keys #{; Token is explicitly blacklisted "just in case"
                                 ; because on start we want the app to read current token from the urlbar instead of storage.
                                 :token})

(defn -wrap-load-from-storage
  "Prevents loading invalid model."
  [original-load-from-storage]
  (fn load-from-storage
    [app loaded-model]
    (if-let [problems (s/check (apply dissoc model/Model -not-persisted-model-keys)
                               loaded-model)]
      (.log js/console "Persisted model is not loaded because it fails the schema check:" problems)
      (original-load-from-storage app loaded-model))))

(defn main
  []
  (let [*model (atom model/initial-model)
        _ (set-validator! *model (partial s/validate model/Model))

        history (aide-history/new-hash-history)
        storage hp/local-storage
        app (-> (aide/object {:*model  *model
                              :history history})

                ; Persistence is added early because it changes the model
                (aide-persistence/add {:storage       storage
                                        :key          :friend-list
                                        :*model       *model
                                        :blacklist    -not-persisted-model-keys
                                        :load-wrapper -wrap-load-from-storage})

                ; Routing events must fire after model is loaded from local storage
                (aide-history/add {:history         history
                                    :*model         *model
                                    :token-key-path [:token]
                                    :on-enter-event events/on-enter})

                ; Logging must be last in order to catch all the events
                aide-logging/add)

        view-model (view-model/view-model (aide-reagent/atom->reaction *model))]
    (-> (view/view history)
        (aide-reagent/connect view-model app)
        (r/render (.getElementById js/document "root")))

    (aide/emit app aide-lifetime/on-start)

    (assoc app :view-model view-model)))

(def app (main))

(defn figwheel-before-jsload
  []
  (aide/emit app aide-lifetime/on-stop))