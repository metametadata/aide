(ns aide.var-event
  "Serializable [[aide.event-api/Event]] which delegates handling to the function stored in a var.

  Deserialization currently is not supported in advanced compilation because function names will be minimized.
  It can be implemented in the future lib versions."
  (:require [aide.event-api :as event-api]
    #?(:cljs
       [clojure.string :as str]))
  #?(:clj
     (:import (clojure.lang Var))))

; It is important to not pass vars (e.g. #'foo) around because in ClojureScript it can lead to leaking var metadata into the compiled JS.
; It bloats the size of the JS bundle and exposes ClojureScript source file paths.
; Note that the instances do not really take the fn value from var. `fn-var-name` is needed for correct serialization/deserialization.
(defrecord VarEvent [name fn fn-var-name]
  event-api/Event
  (handle
    [this object data]
    ((:fn this) object data))

  Object
  (toString
    [_]
    name))

#?(:clj
   (defmethod print-method VarEvent
     [v ^java.io.Writer w]
     (.write w "#aide/VarEvent")
     (.write w (pr-str (dissoc v :fn))))

   :cljs
   (extend-protocol IPrintWithWriter
     VarEvent
     (-pr-writer
       [this w _opts]
       (-write w "#aide/VarEvent")
       (-write w (pr-str (dissoc this :fn))))))

(defn -fn
  [var-name]
  #?(:clj  @(find-var (symbol var-name))
     :cljs (-> var-name
               (str/replace #"/" ".")
               (str/replace #"-" "_")
               js/eval)))

(defn -read-var-event
  [m]
  (-> m
      map->VarEvent
      (assoc :fn (-fn (:fn-var-name m)))))

(def edn-readers {'aide/VarEvent -read-var-event})