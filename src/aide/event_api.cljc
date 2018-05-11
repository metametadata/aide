(ns aide.event-api
  "Event which knows how to handle itself.")

(defprotocol Event
  (handle [_ object data]))