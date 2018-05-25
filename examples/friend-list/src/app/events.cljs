(ns app.events
  (:require [app.api :as api]
            [aide.core :as aide]
            [aide-history.core :as aide-history]
            [goog.functions :as goog-functions]))

(aide/defevent on-search-success
  [app [q friends]]
  (if (= q (:query @(:*model app)))
    (swap! (:*model app) assoc :friends friends)
    (println "ignore response for" (pr-str q)
             "because current query is" (pr-str (:query @(:*model app))))))

(defn -search
  [app q]
  (api/search q #(aide/emit app on-search-success [q %])))

(aide/defevent on-enter
  [app {:keys [token]}]
  (swap! (:*model app) assoc :query token)
  (-search app token))

(def -search-on-input
  (goog-functions/debounce
    (fn [app q]
      (aide-history/push-token (:history app) q {:bypass-on-enter-event? true})
      (-search app q))
    300))

(aide/defevent on-input
  [app q]
  (swap! (:*model app) assoc :query q)
  (-search-on-input app q))