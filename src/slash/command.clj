(ns slash.command)

(defn command-path [{{:keys [name options]} :data}]
  (into
   [name]
   (->> (get options 0 nil)
        (iterate (comp #(get % 0 nil) :options))
        (take-while (comp #{1 2} :type))
        (mapv :name))))

(defn- actual-command? [layer]
  (-> layer :options first #{1 2} not))

(defn command-options [interaction]
  (loop [layer interaction]
    (if (actual-command? layer)
      (zipmap (map (comp keyword :name) (:options layer)) (map :value (:options layer)))
      (recur (-> layer :options first)))))

(defn wrap-options
  [handler]
  (fn [command]
    (handler (cond-> command (not (:option-map command)) (assoc :option-map (command-options command))))))

(defn wrap-path
  [handler]
  (fn [command]
    (handler (cond-> command (not (:path command)) (assoc :path (command-path command))))))

(defn- paths-match? [pattern actual]
  (and
   (= (count pattern) (count actual))
   (->> (map vector pattern actual)
        (remove (comp symbol? first))
        (map (partial apply =))
        (every? true?))))

(defn wrap-check-path
  [handler path & {:keys [prefix-check?]}]
  (fn [{actual-path :path :as command}]
    (when (paths-match? path (cond->> actual-path prefix-check? (take (count path))))
      (handler command))))

(defn- placeholder-positions
  [path]
  (->> path
       (map-indexed vector)
       (filter (comp symbol? second))
       (map first)))

(defmacro command-handler
  {:style/indent 3}
  [path interaction-binding options & body]
  (let [placeholder-indices (placeholder-positions path)
        placeholders (mapv path placeholder-indices)]
    `(-> (fn [{:keys [option-map# path#] :as command#}]
           (let [~placeholders (map path# ~placeholder-indices)
                 ~(if (map? options) options `{:keys [~@options]}) option-map#
                 ~interaction-binding interaction#]
             ~@body))
         wrap-options
         (wrap-check-path ~path)
         wrap-path)))

(def dispatch
  (-> (fn [handlers command]
        (some #(some-> %) (map #(% command) handlers)))
      wrap-path))

(defmacro group
  {:style/indent 1}
  [prefix & handlers]
  (let [placeholder-indices (placeholder-positions prefix)
        placeholders (mapv prefix placeholder-indices)]
    `(-> (fn [{:keys [path#] :as command#}]
           (let [~placeholders (map path# ~placeholder-indices)]
             (dispatch (list ~@handlers) (assoc command# :path (vec (drop ~(count prefix) path#))))))
         (wrap-check-path ~prefix :prefix-check? true)
         wrap-path)))

(defn commands
  [& handlers]
  (-> (partial dispatch handlers)
      wrap-path))

(defmacro defcommands
  {:style/indent 1}
  [symbol & handlers]
  `(def ~symbol (commands ~@handlers)))

(defmacro defhandler
  {:style/indent 1}
  [symbol path interaction-binding options & body]
  `(def ~symbol (command-handler ~path ~interaction-binding ~options ~@body)))
