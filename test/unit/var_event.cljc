(ns unit.var-event
  (:require [aide.var-event :as var-event]
    #?(:clj
            [clojure.edn :as edn]
       :cljs [cljs.reader :as edn])
            [clojure.test :refer [deftest is testing]]))

(defn on-raw-event-fn
  [object data]
  [:--raw-event-- object data])

(deftest raw-var-event-can-be-serialized-as-edn
  (let [value (var-event/map->VarEvent {:name        "unit.var-event/on-foo"
                                        :fn          on-raw-event-fn
                                        :fn-var-name "unit.var-event/on-raw-event-fn"})

        ; act
        serialized-value (pr-str value)

        ; assert
        expected-serialized-value "#aide/VarEvent{:name \"unit.var-event/on-foo\", :fn-var-name \"unit.var-event/on-raw-event-fn\"}"
        _ (is (= expected-serialized-value serialized-value))

        ; act
        deserialized-value (edn/read-string {:readers var-event/edn-readers} serialized-value)]
    ; assert
    (is (= value deserialized-value))
    (is (= [:--raw-event-- :--object-- :--data--] ((:fn deserialized-value) :--object-- :--data--)))))

(deftest toString-returns-var-event-name
  (is (= "event-name" (str (var-event/map->VarEvent {:name        "event-name"
                                                     :fn          :--fn--
                                                     :fn-var-name :--fn-var-name--})))))
