(ns slash.command.structure)

(defn- omission-map [& keyvals]
  (reduce (fn [m [key val]] (cond-> m (some? val) (assoc key val))) {} (partition 2 keyvals)))

(defn command
  [name description & {:keys [default-permission guild-id options]}]
  (omission-map
   :name name
   :description description
   :options options
   :guild_id guild-id
   :default_permission default-permission))

(defn sub-command-group
  [name description & sub-commands]
  (omission-map
   :type 2
   :name name
   :description description
   :options sub-commands))

(defn sub-command
  [name description & {:keys [options]}]
  (omission-map
   :type 1
   :name name
   :description description
   :options options))

(def option-types
  {:string 3
   :integer 4
   :boolean 5
   :user 6
   :channel 7
   :role 8
   :mentionable 9
   :number 10})

(defn option
  [name description type & {:keys [required choices]}]
  (omission-map
   :type (option-types type type)
   :name name
   :description description
   :required required
   :choices choices))

(defn choice
  [name value]
  {:name name
   :value value})
