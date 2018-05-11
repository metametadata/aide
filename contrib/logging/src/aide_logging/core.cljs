(ns aide-logging.core)

(defn add
  "Will print all events to console using the specified prefix string."
  ([object] (add object ""))
  ([object prefix]
   (update object :aide.core/emit
           (fn wrap-emit
             [original-emit]
             (fn emit
               [object event data]
               (try
                 (.group js/console
                         (str "%c" prefix "%c" event " %c" (pr-str data))
                         "font-weight: normal; color: green"
                         "font-weight: bold"
                         "font-weight: normal")
                 (original-emit object event data)

                 ; This clause guarantees that group is closed even in case of exception
                 (finally
                   (.groupEnd js/console))))))))