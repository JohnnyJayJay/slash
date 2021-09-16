(ns slash.util)

(defn omission-map [& keyvals]
  (reduce (fn [m [key val]] (cond-> m (some? val) (assoc key val))) {} (partition 2 keyvals)))
