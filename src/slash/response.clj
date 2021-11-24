(ns slash.response
  "Definitions and utilities for interaction responses.

  Read https://discord.com/developers/docs/interactions/slash-commands#interaction-response-object")

(def pong
  "The PONG response (type 1)"
  {:type 1})

(defn channel-message
  "Respond to an interaction with a message - `data` is the message object (type 4)."
  [data]
  {:type 4
   :data data})

(def deferred-channel-message
  "Defer a message response (type 5)"
  {:type 5})

(def deferred-update-message
  "Defer a message update (only for component interactions - type 6)"
  {:type 6})

(defn update-message
  "Update the message - `data` is the message update (only for component interactions - type 7)"
  [data]
  {:type 7
   :data data})

(defn autocomplete-result
  "Return suggestions for autocompletion (only for autocomplete interactions)."
  [choices]
  {:type 8
   :data {:choices choices}})

(defn ephemeral
  "Takes an interaction response and makes it ephemeral."
  [response]
  (assoc-in response [:data :flags] 64))
