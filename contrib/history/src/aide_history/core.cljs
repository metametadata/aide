(ns aide-history.core
  (:require [aide.core :as aide]
            [aide.lifetime :as aide-lifetime]
            [goog.events :as goog-events]
            [goog.history.EventType :as EventType])
  (:import goog.history.Html5History))

; History
(defprotocol History
  "Object managing browser history."
  (listen [this callback]
          "Starts calling back on history events.
          Callback function signature: `[token navigation? event-data]`, where:

          * `token` - new token
          * `navigation?` - `true` if event was initiated by action in browser, e.g. clicking Back button
          * `event-data` - data which was passed from `replace-token`/`push-token`

          Returns a function which stops listening.")
  (replace-token [this token] [this token event-data]
                 "Replace token and fire an event with additional data passed (data is `nil` if not specified);
                 do nothing if current token is already equal to the specified one.")
  (push-token [this token] [this token event-data]
              "Push token and fire an event with additional data passed (data is `nil` if not specified);
              do nothing if current token is already equal to the specified one.")
  (token [this] "Return current token.")
  (token->href [this token] "Returns the href for the specified token to be used in HTML links."))

; Implementation of History using Closure API
(def ^:dynamic ^:no-doc *-history-event-data* nil)
(defrecord ^:no-doc -History [-goog-history]
  History
  (listen
    [_this callback]
    (let [key (goog-events/listen -goog-history EventType/NAVIGATE #(callback (.-token %)
                                                                              (.-isNavigation %)
                                                                              *-history-event-data*))]
      #(goog-events/unlistenByKey key)))

  (replace-token [this new-token] (replace-token this new-token nil))
  (replace-token
    [this new-token event-data]
    (binding [*-history-event-data* event-data]
      ; Prevent firing an event if token is going to stay the same
      (when (not= (token this) new-token)
        (.replaceToken -goog-history new-token))))

  (push-token [this token] (push-token this token nil))
  (push-token
    [_this token event-data]
    (binding [*-history-event-data* event-data]
      (.setToken -goog-history token)))

  (token
    [_this]
    (.getToken -goog-history))

  (token->href
    [_this token]
    (.getUrl_ -goog-history token)))

(defn new-hash-history
  "For history management using hashes based on onhashchange event. Will not correctly work in Opera Mini: http://caniuse.com/#search=hash"
  []
  (let [history (Html5History.)]
    (.setUseFragment history true)
    (.setEnabled history true)
    (->-History history)))

(defn new-history
  "For history management using pushState. Supported browsers: http://caniuse.com/#search=pushstate"
  []
  (let [history (Html5History.)]
    ; Gets rid of "Uncaught SecurityError: Failed to execute 'pushState' on 'History': A history state object with URL
    ; 'http://active/' cannot be created in a document with origin 'http://localhost:3449' and URL 'http://localhost:3449/'"
    (.setPathPrefix history "")

    (.setUseFragment history false)
    (.setEnabled history true)
    (->-History history)))

; Middleware
(aide/defevent ^:no-doc -on-history-event
  "Extracted as event for easier debugging."
  [{:keys [::-*model ::-token-key-path ::-on-enter-event] :as app} {:keys [token navigation? event-data]}]
  (swap! -*model assoc-in -token-key-path token)

  (when (and (some? -on-enter-event)
             (not (:bypass-on-enter-event? event-data)))
    (aide/emit app -on-enter-event {:token       token
                                    :navigation? navigation?})))

(defn ^:no-doc -wrap-emit
  [original-emit history *model token-key-path]
  (let [*unlisten (atom nil)]
    (fn emit
      [app event data]
      (condp = event
        aide-lifetime/on-start
        (let [original-emit-result (original-emit app event data)]
          (add-watch *model
                     token-key-path
                     (fn [_key _ref old-state new-state]
                       (let [old-token (get-in old-state token-key-path)
                             new-token (get-in new-state token-key-path)]
                         (when (not= old-token new-token)
                           (replace-token history new-token)))))

          (reset! *unlisten
                  (listen history #(aide/emit app -on-history-event {:token       %1
                                                                     :navigation? %2
                                                                     :event-data  %3})))

          ; Initial event
          (aide/emit app -on-history-event {:token       (token history)
                                            :navigation? false
                                            :event-data  nil})

          original-emit-result)

        aide-lifetime/on-stop
        (do
          (when (ifn? @*unlisten)
            (@*unlisten))

          (remove-watch *model token-key-path)

          (original-emit app event data))

        (original-emit app event data)))))

(defn add
  "Applies middleware which syncs app model with browser history.

  After start it begins catching history events and updates token in model accordingly.
  If token changes in model then current url is replaced using the new token.

  Optional `[on-enter-event {:token :navigation?}]` is emitted on every token change.
  navigation? flag is set to true only if event was triggered by browser navigation (e.g. clicking Back button).
  You can also force not emitting on-enter-event by by passing `{:bypass-on-enter-event? true}`
  to [[HistoryProtocol]]'s `replace-token`/`push-token` methods.

  Initial on-enter-event is emitted with navigation? set to false."
  [app {:keys [history *model token-key-path on-enter-event]
        :or   {on-enter-event nil}}]
  {:pre [(satisfies? History history) (some? *model) (some? token-key-path)]}
  (-> app
      (update ::aide/emit -wrap-emit history *model token-key-path)

      ; Inject options for future use
      (merge {::-*model         *model
              ::-token-key-path token-key-path
              ::-on-enter-event on-enter-event})))

; Link
(defn ^:no-doc -pure-click?
  "Returns false if the user did a middle-click, right-click, or used a modifier."
  [e]
  (not (or (.-altKey e)
           (.-ctrlKey e)
           (.-metaKey e)
           (.-shiftKey e)
           (not (zero? (.-button e))))))

(defn ^:no-doc -on-click
  [e history token replace?]
  (when (-pure-click? e)
    (.preventDefault e)
    (if replace?
      (replace-token history token)
      (push-token history token))))

(defn link-attrs
  "Returns Reagent attrs map ({:href ... :on-click ...}) for a link element which changes current URL without sending request to server.
  Useful for cases when you can't put [[link]] into UI component which is already an <a> (e.g. react-bootstrap's NavItem)."
  [history token {:keys [replace?] :as attrs}]
  (-> attrs
      (dissoc :replace?)
      (merge {:href     (token->href history token)
              :on-click #(-on-click % history token replace?)})))

(defn link
  "Link Reagent component which changes current URL without sending request to server.
  Will replace current token instead of pushing if `:replace?` attribute is `true` (attribute is `false` by default).
  If history middleware is added then clicking the link will produce `on-enter` event with navigation? set to false.
  Essentially, it's just an `<a>` with [[link-attrs]]."
  [history token {:keys [replace?] :as attrs} & body]
  (into [:a (link-attrs history token attrs)] body))