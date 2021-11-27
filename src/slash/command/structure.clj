(ns slash.command.structure
  "Functions to make command definition easier.

  Read https://discord.com/developers/docs/interactions/slash-commands first to understand the structure of slash commands."
  (:require [slash.util :refer [omission-map]]))

(def command-types
  "Map of command type names (keywords) to numerical command type identifiers."
  {:chat-input 1
   :user 2
   :message 3})

(defn command
  "Create a top level command.

  See https://discord.com/developers/docs/interactions/slash-commands#application-command-object-application-command-structure.
  `:type` must be one of the keys in [[command-types]], if given."
  [name description & {:keys [default-permission guild-id options type]}]
  (omission-map
   :name name
   :description description
   :options options
   :guild_id guild-id
   :default_permission default-permission
   :type (some-> type command-types)))

(defn message-command
  "Create a top level message command.

  See https://discord.com/developers/docs/interactions/application-commands#message-commands."
  [name & {:keys [default-permission]}]
  (command name "" :default-permission default-permission :type :message))

(defn user-command
  "Create a top level user command.

  See https://discord.com/developers/docs/interactions/application-commands#user-commands."
  [name & {:keys [default-permission]}]
  (command name "" :default-permission default-permission :type :user))

(defn sub-command-group
  "Create a sub command group option."
  [name description & sub-commands]
  (omission-map
   :type 2
   :name name
   :description description
   :options sub-commands))

(defn sub-command
  "Create a sub command option."
  [name description & {:keys [options]}]
  (omission-map
   :type 1
   :name name
   :description description
   :options options))

(def option-types
  "Map of option type names (keywords) to their numerical identifiers."
  {:string 3
   :integer 4
   :boolean 5
   :user 6
   :channel 7
   :role 8
   :mentionable 9
   :number 10})

(def channel-types
  "Map of channel type names (keywords) to numerical channel type identifiers."
  {:guild-text 0
   :dm 1
   :guild-voice 2
   :group-dm 3
   :guild-category 4
   :guild-news 5
   :guild-store 6
   :guild-news-thread 10
   :guild-public-thread 11
   :guild-private-thread 12
   :guild-stage-voice 13})

(defn option
  "Create a regular option.

  `:channel-types` must be a collection of keys from [[channel-types]], if given.
  This may only be set when `type` is `:channel`."
  [name description type & {:keys [required choices] ch-types :channel-types}]
  (omission-map
   :type (option-types type type)
   :name name
   :description description
   :required required
   :choices choices
   :channel_types (some->> ch-types (map channel-types))))

(defn choice
  "Create an option choice for a choice set."
  [name value]
  {:name name
   :value value})
