(ns slash.webhook
  (:require [slash.response :refer [pong]]))

(defn interaction-not-supported
  [{:keys [type] :as _interaction}]
  {:status 400
   :headers {"Content-Type" "text/plain"}
   :body (str "Interactions of type " type " are not supported")})

(def webhook-defaults
  {:ping (constantly pong)
   :application-command interaction-not-supported
   :message-component interaction-not-supported})
