(ns slash.command
  "The command namespace contains functions to create command handlers and dispatch commands.")

(defn path
  "Given a command, returns the fully qualified command name as a vector.

  The command is the data associated with an interaction create event of type 2 as Clojure data.
  Examples (what the command data represents => what this function returns):
  - `/foo bar baz` => [\"foo\" \"bar\" \"baz\"]
  - `/foo` => [\"foo\"]"
  [{:keys [name options] :as _command}]
  (into
   [name]
   (->> (get options 0 nil)
        (iterate (comp #(get % 0 nil) :options))
        (take-while (comp #{1 2} :type))
        (mapv :name))))

(defn- actual-command? [layer]
  (-> layer :options first :type #{1 2} not))

(defn option-map
  "Returns the options of a command as a map of keywords -> values.

  The command is the data associated with an interaction create event of type 2 as Clojure data.
  'options' here means the options the user sets, like `baz` in `/foo bar baz: 3`, but not `bar`."
  [command]
  (loop [layer command]
    (if (actual-command? layer)
      (zipmap (map (comp keyword :name) (:options layer)) (map :value (:options layer)))
      (recur (-> layer :options first)))))

(defn wrap-options
  "Middleware that attaches the `:option-map` (obtained by [[option-map]]) to the command, if not already present."
  [handler]
  (fn [{command :data :as interaction}]
    (handler (cond-> interaction
               (not (contains? command :option-map))
               (assoc-in [:data :option-map] (option-map command))))))

(defn wrap-path
  "Middleware that attaches the `:path` (obtained by [[path]]) to the command, if not already present."
  [handler]
  (fn [{command :data :as interaction}]
    (handler (cond-> interaction
               (not (contains? command :path))
               (assoc-in [:data :path] (path command))))))

(defn- paths-match? [pattern actual]
  (and
   (= (count pattern) (count actual))
   (->> (map vector pattern actual)
        (remove (comp symbol? first))
        (map (partial apply =))
        (every? true?))))

(defn wrap-check-path
  "Middleware that delegates to the handler only if the command `:path` matches the given `path` pattern.

  `path` is a vector of strings (literal matches) and symbols (placeholders that match any value).
  Optionally, `:prefix-check? true` can be set, in which case it will only be checked whether `path` prefixes the command path.

  Must run after [[wrap-path]]."
  [handler path & {:keys [prefix-check?]}]
  (fn [{{actual-path :path} :data :as interaction}]
    (when (paths-match? path (cond->> actual-path prefix-check? (take (count path))))
      (handler interaction))))

(defn- placeholder-positions
  [path]
  (->> path
       (map-indexed vector)
       (filter (comp symbol? second))
       (map first)))

(defmacro let-placeholders
  {:style/indent 2}
  [pattern path & body]
  (let [placeholder-indices (placeholder-positions pattern)
        placeholders (mapv pattern placeholder-indices)]
    `(let [~placeholders (map ~path (list ~@placeholder-indices))]
       ~@body)))

(defn- replace-symbols [pattern]
  (mapv #(if (symbol? %) ''_ %) pattern))

(defmacro handler
  "A macro to generate a command handler that will accept commands matching the given pattern.

  `pattern` is a vector of literals (strings) and placeholders (symbols).
  Placeholders will match and be bound to any string at that position in the command path.
  `interaction-binding` is a binding that will be bound to the entire interaction object.
  `options` is either a vector, in which case  the symbols in that vector will be bound to the options with corresponding names
  - otherwise, it will be bound to the command's [[option-map]] directly.
  `body` is the command logic. It may access any of the bound symbols above.

  The function returned by this already has the [[wrap-options]], [[wrap-check-path]] and [[wrap-path]] middlewares applied."
  {:style/indent 3}
  [pattern interaction-binding options & body]
  `(-> (fn [{{option-map# :option-map path# :path} :data :as interaction#}]
         (let-placeholders ~pattern path#
           (let [~(if (vector? options) `{:keys [~@options]} options) option-map#
                 ~interaction-binding interaction#]
             ~@body)))
       wrap-options
       (wrap-check-path ~(replace-symbols pattern))
       wrap-path))

(defmacro defhandler
  "Utility macro for `(def my-handler (handler ...))` (see [[handler]])"
  {:style/indent 1}
  [symbol path interaction-binding options & body]
  `(def ~symbol (handler ~path ~interaction-binding ~options ~@body)))

(defn dispatch
  "A function to dispatch a command to a list of handlers.

  Takes two arguments: `handlers` (a list of command handler functions) and `interaction`,
  the interaction of a command execution.

  Each handler will be run until one is found that does not return `nil`."
  [handlers interaction]
  (some #(some-> %) (map #(% interaction) handlers)))

(defmacro group
  "A macro to combine multiple handlers into one under a common prefix.

  `prefix` is a pattern like in [[handler]].
  `handlers` are command handler functions."
  {:style/indent 1}
  [prefix & handlers]
  `(-> (fn [{{path# :path} :data :as interaction#}]
         (let-placeholders ~prefix path#
           (dispatch (list ~@handlers) (assoc-in interaction# [:data :path] (vec (drop ~(count prefix) path#))))))
       (wrap-check-path ~(replace-symbols prefix) :prefix-check? true)
       wrap-path))

(defn paths
  "Function to combine multiple handlers into one by dispatching on them using [[dispatch]]."
  [& handlers]
  (-> (partial dispatch handlers)
      wrap-path))

(defmacro defpaths
  "Utility macro for `(def symbol (paths handlers))` (see [[paths]])"
  {:style/indent 1}
  [symbol & handlers]
  `(def ~symbol (paths ~@handlers)))
