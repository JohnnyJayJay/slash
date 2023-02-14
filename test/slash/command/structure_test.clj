(ns slash.command.structure-test
  (:require [slash.command.structure :refer :all]
            [clojure.test :refer :all]))

(deftest command-test
  (is (= {:name "foo"
          :description "bar"
          :options [:baz :quz]
          :default_permission false
          :default_member_permissions "0"}
         (command "foo" "bar" :default-permission false :default-member-permissions "0" :options [:baz :quz]))))

(deftest sub-command-group-test
  (is (= {:type 2
          :name "foo"
          :description "bar"
          :options [:baz :quz]}
         (sub-command-group "foo" "bar" :baz :quz))))

(deftest sub-command-test
  (is (= {:type 1
          :name "foo"
          :description "bar"
          :options [:baz :quz]}
         (sub-command "foo" "bar" :options [:baz :quz]))))

(deftest option-test
  (is (= {:type 3
          :name "baz"
          :description "quz"
          :required true
          :autocomplete true}
         (option "baz" "quz" :string :required true :autocomplete true)))
  (is (= {:type 6
          :name "baz"
          :description "quz"
          :choices [:foo :bar]}
         (option "baz" "quz" :user :choices [:foo :bar])))
  (is (= {:type 4
          :name "baz"
          :description "quz"
          :min_value -56
          :max_value 42}
         (option "baz" "quz" :integer :min-value -56 :max-value 42)))
  (is (= {:type 7
          :name "baz"
          :description "quz"
          :channel_types [0 2]}
         (option "baz" "quz" :channel :channel-types [:guild-text :guild-voice]))))

(deftest choice-test
  (is (= {:name "foo"
          :value "bar"}
         (choice "foo" "bar")))
  (is (= {:name "baz"
          :value 5}
         (choice "baz" 5))))
