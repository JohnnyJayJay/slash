(ns slash.command.structure
  "Functions to make command definition easier.

  Read https://discord.com/developers/docs/interactions/slash-commands first to understand the structure of slash commands."
  (:require [slash.util :refer [omission-map]]))

(defn command
  "Create a top level command.

  See https://discord.com/developers/docs/interactions/slash-commands#application-command-object-application-command-structure"
  [name description & {:keys [default-permission guild-id options]}]
  (omission-map
   :name name
   :description description
   :options options
   :guild_id guild-id
   :default_permission default-permission))

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

(defn option
  "Create a regular option."
  [name description type & {:keys [required choices]}]
  (omission-map
   :type (option-types type type)
   :name name
   :description description
   :required required
   :choices choices))

(defn choice
  "Create an option choice for a choice set."
  [name value]
  {:name name
   :value value})
