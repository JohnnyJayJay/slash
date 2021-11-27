(ns slash.webhook
  "Slash functionality for receiving interactions via outgoing webhook."
  (:require [slash.response :refer [pong]]))

(defn interaction-not-supported
  "A ring-compliant handler that takes an interaction and returns a Bad Request response."
  [{:keys [type] :as _interaction}]
  {:status 400
   :headers {"Content-Type" "text/plain"}
   :body (str "Interactions of type " type " are not supported")})

(def webhook-defaults
  "Default webhook interaction handlers.

  Returns a 400 Bad Request response for all interactions except PING, for which it returns a PONG response."
  {:ping (constantly pong)
   :application-command interaction-not-supported
   :message-component interaction-not-supported
   :application-command-autocomplete interaction-not-supported})
