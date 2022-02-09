(ns slash.component.structure-test
  (:require [slash.component.structure :refer :all]
            [clojure.test :refer :all]))


(deftest action-row-test
  (is (= {:type 1
          :components [1 2 3]}
         (action-row 1 2 3))))

(deftest link-button-test
  (is (= {:type 2
          :style 5
          :url "http://example.com"
          :label "Click me"
          :emoji {:name "foo" :id "bar"}
          :disabled true}
         (link-button
          "http://example.com"
          :label "Click me"
          :emoji {:name "foo" :id "bar"}
          :disabled true))))

(deftest button-test
  (is (= {:type 2
          :style 3
          :custom_id "xyz"
          :label "heya"
          :emoji {:name "foo" :id "bar"}}
         (button
          :success
          "xyz"
          :label "heya"
          :emoji {:name "foo" :id "bar"})))
  (is (= {:type 2
          :style 4
          :custom_id "abc"
          :label "xyz"
          :disabled true}
         (button
          :danger
          "abc"
          :label "xyz"
          :disabled true))))

(deftest select-menu-test
  (is (= {:type 3
          :custom_id "xyz"
          :options [1 2 3]
          :placeholder "Lorem Ipsum"
          :min_values 3}
         (select-menu
          "xyz"
          [1 2 3]
          :placeholder "Lorem Ipsum"
          :min-values 3)))
  (is (= {:type 3
          :custom_id "abc"
          :options [3 2 1]
          :max_values 5
          :disabled true}
         (select-menu
          "abc"
          [3 2 1]
          :max-values 5
          :disabled true))))

(deftest select-option-test
  (is (= {:label "A"
          :value "test"
          :description "Lorem Ipsum"
          :emoji {:id "foo" :name "bar"}}
         (select-option
          "A"
          "test"
          :description "Lorem Ipsum"
          :emoji {:id "foo" :name "bar"})))
  (is (= {:label "B"
          :value 42
          :default true}
         (select-option
          "B"
          42
          :default true))))

(deftest text-input-test
  (is (= {:type 4
          :style 1
          :custom_id "xyz"
          :label "Enter x"
          :value "Hello"
          :max_length 50
          :required true}
         (text-input
          :short
          "xyz"
          "Enter x"
          :max-length 50
          :required true
          :value "Hello")))
  (is (= {:type 4
          :style 2
          :custom_id "abc"
          :label "Enter y"
          :placeholder "foo bar"
          :min_length 20}
         (text-input
          :paragraph
          "abc"
          "Enter y"
          :min-length 20
          :placeholder "foo bar"))))
