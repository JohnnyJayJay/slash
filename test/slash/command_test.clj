(ns slash.command-test
  (:require [slash.command :refer :all]
            [clojure.test :refer :all]))

(def foo-bar-baz
  {:name "foo"
    :options
    [{:type 2
      :name "bar"
      :options
      [{:type 1
        :name "baz"
        :options
        [{:type 3
          :name "hello"
          :value "world"
          :focused true}
         {:type 4
          :name "num"
          :value 56}]}]}]})

(def foo
  {:name "foo"
    :options
    [{:type 5
      :name "opt"
      :value true}
     {:type 7
      :name "chan"
      :value {:id "123456789"}}]})

(def foo-bar
  {:name "foo"
    :options
    [{:type 1
      :name "bar"}]})

(deftest option-key-test
  (is (= "1foo" (option-key "1foo")))
  (is (= "123" (option-key "123")))
  (is (= :crazy-stuff (option-key "crazy-stuff")))
  (is (= :fo1o (option-key "fo1o"))))

(deftest path-test
  (is (= ["foo" "bar" "baz"] (path foo-bar-baz)))
  (is (= ["foo"] (path foo)))
  (is (= ["foo" "bar"] (path foo-bar))))

(deftest option-map-test
  (testing "Options exist"
    (is (= {:hello "world" :num 56} (option-map foo-bar-baz)))
    (is (= {:opt true :chan {:id "123456789"}} (option-map foo))))
  (testing "Options don't exist"
    (is (= {} (option-map foo-bar)))))

(deftest full-option-map-test
  (testing "Options exist"
    (is (= {:hello {:type 3, :name "hello", :value "world", :focused true}, :num {:type 4, :name "num", :value 56}}
           (full-option-map foo-bar-baz)))
    (is (= {:opt {:type 5, :name "opt", :value true}, :chan {:type 7, :name "chan", :value {:id "123456789"}}}
           (full-option-map foo))))
  (testing "Options don't exist"
    (is (= {} (full-option-map foo-bar)))))

(deftest focused-option-test
  (testing "Focused option"
    (is (= :hello (focused-option (-> foo-bar-baz :options first :options first :options)))))
  (testing "No focused option"
    (is (nil? (focused-option (:options foo))))))

(deftest option-map-mw-test
  (letfn [(handler [{{:keys [option-map full-option-map focused-option]} :data}]
            [option-map full-option-map focused-option])]
    (is (= [{:hello "world" :num 56}
            {:hello {:type 3, :name "hello", :value "world", :focused true}, :num {:type 4, :name "num", :value 56}}
            :hello]
           ((wrap-options handler) {:data foo-bar-baz})))))

(deftest path-mw-test
  (letfn [(handler [{{:keys [path]} :data}] path)]
    (is (= ["foo" "bar" "baz"] ((wrap-path handler) {:data foo-bar-baz})))))

(defn handler-path [handler path]
  (-> handler (wrap-check-path path) wrap-path))

(defn handler-prefix-path [handler path]
  (-> handler (wrap-check-path path :prefix-check? true) wrap-path))

(deftest check-path-mw-test
  (letfn [(handler [_] true)]
    (testing "literal matching"
      (is ((handler-path handler ["foo" "bar" "baz"]) {:data foo-bar-baz}))
      (is ((handler-path handler ["foo"]) {:data foo}))
      (is (not ((handler-path handler ["foo" "bar" "baz"]) {:data foo-bar})))
      (is (not ((handler-path handler ["foo" "quz"]) {:data foo-bar}))))
    (testing "placeholder matching"
      (is ((handler-path handler ['_]) {:data foo}))
      (is ((handler-path handler ["foo" 'sym "baz"]) {:data foo-bar-baz}))
      (is (not ((handler-path handler ["foo" '_ '_]) {:data foo-bar}))))
    (testing "prefix matching"
      (is ((handler-prefix-path handler ["foo" '_]) {:data foo-bar-baz}))
      (is ((handler-prefix-path handler ["foo"]) {:data foo}))
      (is ((handler-prefix-path handler []) {:data foo-bar}))
      (is (not ((handler-prefix-path handler ["foo" "baz"]) {:data foo-bar-baz}))))))

(def foo-_-baz-handler
  (handler ["foo" bar "baz"] all [hello num]
    [bar (update all :data dissoc :option-map :full-option-map :focused-option :path) hello num]))

(def foo-handler
  (handler ["foo"] _ {:keys [opt] :as options}
    [opt options]))

(def full-foo-handler
  (handler ["foo"] _ ^:full [opt]
    opt))

(deftest handler-test
  (testing "option vector"
    (is (nil? (foo-_-baz-handler {:data foo})))
    (is (= ["bar" {:data foo-bar-baz} "world" 56] (foo-_-baz-handler {:data foo-bar-baz}))))
  (testing "option binding"
    (is (nil? (foo-handler {:data foo-bar})))
    (is (= [true {:opt true :chan {:id "123456789"}}] (foo-handler {:data foo}))))
  (testing "full option binding"
    (is (= {:type 5, :name "opt", :value true} (full-foo-handler {:data foo})))))

(def num-handlers
  (mapv handler-path
        (map constantly (range))
        [["foo" "nope"]
         ["foo" "bar"]
         ["foo"]
         ["foo" '_]
         ["lol"]]))

(def dispatcher
  (partial dispatch num-handlers))

(deftest dispatch-test
  (is (= 1 (dispatcher {:data foo-bar})))
  (is (= 2 (dispatcher {:data foo})))
  (is (nil? (dispatcher {:data foo-bar-baz}))))

(def group-handler
  (group ["foo" x]
    (handler ["baz"] _ _
      x)))

(deftest group-test
  (is (= "bar" (group-handler {:data foo-bar-baz}))))

(def paths-dispatcher (apply paths num-handlers))

(deftest paths-test
  (is (= 1 (paths-dispatcher {:data foo-bar})))
  (is (= 2 (paths-dispatcher {:data foo})))
  (is (nil? (paths-dispatcher {:data foo-bar-baz}))))
