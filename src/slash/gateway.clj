(ns slash.gateway
  "Slash functionality for receiving interactions via the Discord gateway.")

(def nop
  "No operation function. Returns `nil`."
  (constantly nil))

(def gateway-defaults
  "Default gateway interaction handlers.

  The interaction handlers in this map simply don't do anything."
  {:ping nop
   :application-command nop
   :message-component nop
   :application-command-autocomplete nop
   :modal-submit nop})

(defn wrap-response-return
  "Middleware that takes the return value of an interaction handler and consumes it in some way.

  This is useful for responding to interactions via REST.
  The interaction handlers can simply return their response and this middleware
  uses the given `respond-fn` to send it to Discord.

  `respond-fn` is a function of 3 parameters: interaction id, interaction token and interaction response."
  [handler respond-fn]
  (fn [{:keys [id token] :as interaction}]
    (when-some [response (handler interaction)]
      (respond-fn id token response))))
