(ns aide.core
  (:require [aide.event-api :as event-api]
            [aide.var-event :as var-event])
  #?(:cljs (:require-macros
             [aide.core :refer
              [defevent*
               defevent]])))

(defn emit
  "Emits the event into the specified object. Returns the event handling result (depends on the object and/or event).
  Object is a map that contains a function of `[object event data]` at `::emit` key.
  Event `data` is nil by default.

  Essentially, this is a shorthand/syntax sugar on top of `(::emit object)`."
  ([object event] (emit object event nil))
  ([object event data] ((::emit object) object event data)))

(defn default-emit
  "Emit implementation that delegates handling to the [[aide.event-api/Event]] instance itself.
  Most likely you don't need to call this function directly. See [[emit]] instead."
  [object event data]
  (event-api/handle event object data))

(defn object
  "Creates an object from the specified map using [[default-emit]] by default.

  You can provide a custom `::emit` to change the semantics of event emitting.
  E.g. to support some new kind of events (e.g. keywords + multi-methods).
  But make sure it still supports [[aide.event-api/Event]] instances
  in order to make it compatible with third-party packages and standard [[on-start]]/[[on-stop]] events."
  [m]
  (merge {::emit default-emit} m))

#?(:clj
   (defn -cljs-env?
     "Take the &env from a macro, and tell whether we are expanding into cljs.
      Source: https://groups.google.com/d/msg/clojurescript/iBY5HaQda4A/w1lAQi9_AwsJ"
     [env]
     (boolean (:ns env))))

#?(:clj
   (defmacro defevent*
     "The same as [[defevent]] but allows specifying a custom defn macro (e.g. `schema.core/defn`)."
     [defn-sym sym & fdecl]
     (let [current-ns (if (-cljs-env? &env)
                        (:name (:ns &env))
                        *ns*)
           name (str (str current-ns) "/" (str sym))
           fn-sym (symbol (str "--" sym))
           fn-var-name (str (str current-ns) "/" (str fn-sym))]
       `(do
          (~defn-sym ~fn-sym ~@fdecl)
          (def ~sym (var-event/map->VarEvent {:name        ~name
                                              :fn          ~fn-sym
                                              :fn-var-name ~fn-var-name}))))))

#?(:clj
   (defmacro defevent
     "defn-like macro that creates a serializable [[aide.event-api/Event]] from the function.
     Returns a var with [[aide.var-event/VarEvent]] value."
     [sym & fdecl]
     `(defevent* defn ~sym ~@fdecl)))