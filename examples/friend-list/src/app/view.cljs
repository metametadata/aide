(ns app.view
  (:require [app.events :as events]
            [aide-history.core :as aide-history]
            [reagent.core :as r]))

(defn -search-field
  [_query _emit]
  (r/create-class {:reagent-render
                   (fn [query emit]
                     [:input {:type        "search"
                              :placeholder "Search friends..."
                              :value       query
                              :on-change   #(emit events/on-input (.. % -target -value))}])

                   :component-did-update
                   (fn [this] (.focus (r/dom-node this)))

                   :component-did-mount
                   (fn [this] (.focus (r/dom-node this)))}))

(defn -quick-search
  [history]
  [:div
   "Quick search: "
   [aide-history/link history "Bruce" {} "Bruce"]
   ", "
   [aide-history/link history "Clark" {} "Clark"]])

(defn -friend-list
  [friends]
  [:ul {:style {:list-style-type "none"
                :padding-left    0}}
   (for [f friends]
     ^{:key (:id f)}
     [:li {:style {:font-size 17}}
      [:strong (:name f)] " " (:username f)])])

(defn view
  [history]
  (fn view
    [view-model emit]
    [:div
     [-search-field @(:*query view-model) emit]
     [-quick-search history]
     [-friend-list @(:*friends view-model)]]))