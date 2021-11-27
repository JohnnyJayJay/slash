(ns slash.core
  "Core namespace.")

(def interaction-types
  "A map of interaction type code -> interaction type name keyword.

  See https://discord.com/developers/docs/interactions/slash-commands#interaction-object-interaction-request-type"
  {1 :ping
   2 :application-command
   3 :message-component
   4 :application-command-autocomplete})

(defn route-interaction
  "Takes a handler map and an interaction and routes the interaction to the correct handler.

  The handler map should map each interaction type to one handler function.
  The handler functions take the interaction as a parameter.
  See [[slash.gateway/gateway-defaults]] and [[slash.webhook/webhook-defaults]] for default handler maps.

  The interaction object passed to this function must be given as a Clojure map with keywords as keys."
  [handlers {:keys [type] :as interaction}]
  ((-> type interaction-types handlers) interaction))
