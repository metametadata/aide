(ns app.model
  (:require [schema.core :as s]))

(def Friend
  {:id       s/Int
   :name     s/Str
   :username s/Str})

(s/defschema Model
  {:query   s/Str
   :friends [Friend]
   :token   s/Str})

(def initial-model {:query   ""
                    :friends []
                    :token   ""})
