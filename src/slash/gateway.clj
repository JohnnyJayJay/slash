(ns slash.gateway)

(def nop (constantly nil))

(def gateway-defaults
  {:ping nop
   :application-command nop
   :message-component nop})

(defn wrap-response-return
  [handler respond-fn]
  (fn [{:keys [id token] :as interaction}]
    (when-some [response (handler interaction)]
      (respond-fn id token response))))
