(ns slash.core)

(def interaction-types
  {1 :ping
   2 :application-command
   3 :message-component})

(defn route-interaction
  [handlers {:keys [type data] :as _interaction}]
  ((-> type interaction-types handlers) data))

