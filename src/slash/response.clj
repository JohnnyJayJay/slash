(ns slash.response)

(def pong
  {:type 1})

(defn channel-message [data]
  {:type 4
   :data data})

(def deferred-channel-message
  {:type 5})

(def deferred-update-message
  {:type 6})

(defn update-message [data]
  {:type 7
   :data data})

(defn ephemeral [response]
  (assoc-in response [:data :flags] 64))
