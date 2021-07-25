(ns slash.gateway-test
  (:require [slash.gateway :refer :all]
            [clojure.test :refer :all]))

(deftest nop-test
  (is (nil? (nop :arg))))

(def handler (constantly "foo"))

(def respond-fn (partial str "bar"))

(deftest return-mw-test
  (is (= "barfoo" ((wrap-response-return handler respond-fn) :any))))
