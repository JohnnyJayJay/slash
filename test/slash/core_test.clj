(ns slash.core-test
  (:require [clojure.test :refer :all]
            [slash.core :refer :all]))

(def handlers
  {:ping (constantly :ping)
   :application-command (constantly :cmd)
   :message-component (constantly :cmp)
   :application-command-autocomplete (constantly :ac)})

(deftest routing-test
  (testing "Interaction handler routing"
    (is (= :ping (route-interaction handlers {:type 1})))
    (is (= :cmd (route-interaction handlers {:type 2})))
    (is (= :cmp (route-interaction handlers {:type 3})))
    (is (= :ac (route-interaction handlers {:type 4}))))
  (testing "Interaction handler arguments"
    (is (= {:type 2 :data :foo} (route-interaction (constantly identity) {:type 2 :data :foo})))))
