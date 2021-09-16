(ns slash.component.structure
  "Functions to make component creation easier.

  Read https://discord.com/developers/docs/interactions/message-components first to understand the structure of message components."
  (:require [slash.util :refer [omission-map]]))

(defn action-row
  "Create an action row containing other components.

  See https://discord.com/developers/docs/interactions/message-components#action-rows for more info."
  [& components]
  {:type 1
   :components components})

(defn link-button
  "Create a button that links to a URL."
  [url & {:keys [label emoji disabled]}]
  (omission-map
   :type 2
   :style 5
   :url url
   :label label
   :emoji emoji
   :disabled disabled))

(def button-styles
  "Map of button style names (keywords) to their numerical identifiers."
  {:primary 1
   :secondary 2
   :success 3
   :danger 4})

(defn button
  "Create a regular interaction button.

  See https://discord.com/developers/docs/interactions/message-components#buttons for more info."
  [style custom-id & {:keys [label emoji disabled]}]
  (omission-map
   :type 2
   :style (button-styles style)
   :custom_id custom-id
   :label label
   :emoji emoji
   :disabled disabled))

(defn select-menu
  "Create a select menu.

  See https://discord.com/developers/docs/interactions/message-components#select-menus for more info."
  [custom-id options & {:keys [placeholder min-values max-values disabled]}]
  (omission-map
   :type 3
   :custom_id custom-id
   :options options
   :placeholder placeholder
   :min_values min-values
   :max_values max-values
   :disabled disabled))

(defn select-option
  "Create an option for a select menu."
  [label value & {:keys [description emoji default]}]
  (omission-map
   :label label
   :value value
   :description description
   :emoji emoji
   :default default))
