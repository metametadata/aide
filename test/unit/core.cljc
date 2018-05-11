(ns unit.core
  (:require [aide.core :as aide]
            [aide.var-event :as var-event]
    #?(:clj
            [clojure.edn :as edn]
       :cljs [cljs.reader :as edn])
            [clojure.test :refer [deftest is testing]]))

(deftest defevent-returns-VarEvent-instance-var
  ; act
  (let [actual (aide/defevent my-event [object data] [:--my-event-- object data])]
    ; assert
    #?(:clj
       (do
         (is (var? actual))
         (is (instance? aide.var_event.VarEvent @actual)))

       :cljs
       ; Vars are absent in ClojureScript
       (is (instance? var-event/VarEvent actual)))))

(aide/defevent on-foo
  [object data]
  [:--foo-- object data])

(deftest defevent-event-can-be-serialized-as-edn
  (let [value on-foo

        ; act
        serialized-value (pr-str value)

        ; assert
        expected-serialized-value "#aide/VarEvent{:name \"unit.core/on-foo\", :fn-var-name \"unit.core/--on-foo\"}"
        _ (is (= expected-serialized-value serialized-value))

        ; act
        deserialized-value (edn/read-string {:readers var-event/edn-readers} serialized-value)]
    ; assert
    (is (= value deserialized-value))
    (is (= [:--foo-- :--object-- :--data--] ((:fn deserialized-value) :--object-- :--data--)))))

(aide/defevent ^{:--var-meta-- true} on-with-var-meta
  [_object _data])

(deftest defevent-supports-var-metadata
  (is (:--var-meta-- (meta #'on-with-var-meta))))