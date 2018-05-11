(ns aide.lifetime
  "Standard events to support a common object lifetime pattern (e.g. for app middleware). They do nothing by default."
  (:require [aide.core :as aide]))

(aide/defevent on-start
  [_object _data])

(aide/defevent on-stop
  [_object _data])