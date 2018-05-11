(ns aide-persistence.core
  (:require [aide.core :as aide]
            [aide.lifetime :as aide-lifetime]))

(defn ^:no-doc -whitelist
  "Removes blacklisted keys from the specified map."
  [m blacklist]
  (apply dissoc m blacklist))

(defn ^:no-doc -save
  [storage key model]
  (assoc! storage key model))

(aide/defevent ^:no-doc -on-load-from-storage
  "Extracted as event for easier debugging."
  [app loaded-model]
  (let [*model (::-*model app)
        new-model (merge loaded-model (select-keys @*model (::-blacklist app)))]
    (reset! *model new-model)))

(defn ^:no-doc -load-from-storage
  [app loaded-model]
  (aide/emit app -on-load-from-storage loaded-model))

(defn ^:no-doc -wrap-emit
  [original-emit {:keys [storage key *model blacklist load-wrapper]}]
  (fn emit
    [app event data]
    (condp = event
      aide-lifetime/on-start
      (let [original-emit-result (original-emit app event data)]
        ; Load
        (let [loaded-model (get storage key :not-found)]
          (when (not= loaded-model :not-found)
            ((load-wrapper -load-from-storage) app loaded-model)))

        ; Save for the first time and on every future change
        (-save storage key (-whitelist @*model blacklist))
        (add-watch *model
                   [::watch key]                            ; Unique key
                   (fn [_key _ref old-state new-state]
                     (let [old-state (-whitelist old-state blacklist)
                           new-state (-whitelist new-state blacklist)]
                       (when (not= old-state new-state)
                         (-save storage key new-state)))))

        original-emit-result)

      aide-lifetime/on-stop
      (do
        (remove-watch *model [::watch key])
        (original-emit app event data))

      (original-emit app event data))))

(defn add
  "On start the middleware will load model from the specified storage.
  Saves model into storage on every change.

  Storage is expected to be a transient map (e.g. as provided by [hodgepodge](https://github.com/funcool/hodgepodge)).

  Optional `:blacklist` set should contain model keys which will not be saved and loaded.

  Optional `:load-wrapper` allows decorating model update function. E.g. it's possible to cancel loading based on loaded data:

  ```clj
  (defn -wrap-load-from-storage
    [original-load-from-storage]
    (fn load-from-storage
      [app loaded-model]
      (if-let [problems (s/check ModelSchema loaded-model)]
        (.log js/console \"Persisted model is not loaded because it fails the schema check:\" problems)
        (original-load-from-storage app loaded-model))))
  ```"
  ([app {:keys [storage key *model blacklist load-wrapper]
         :or   {blacklist    #{}
                load-wrapper identity}
         :as   options}]
   {:pre [(satisfies? ITransientAssociative storage) (satisfies? ILookup storage)
          (some? key) (some? *model) (set? blacklist) (ifn? load-wrapper)]}
   (-> app
       (update ::aide/emit -wrap-emit options)

       ; Inject options for future use
       (merge {::-*model    *model
               ::-blacklist blacklist}))))