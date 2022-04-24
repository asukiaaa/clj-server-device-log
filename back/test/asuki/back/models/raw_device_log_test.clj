(ns asuki.back.models.raw-device-log-test
  (:require [clojure.test :refer :all]
            [asuki.back.models.raw-device-log :refer :all]))

#_(deftest a-test
    (testing "FIXME, I fail."
      (is (= 0 1))))

(deftest test-filter-key
  (testing "ignore invalid key"
    (is (= (filter-key "some-unknown-key") nil))))

(deftest test-build-target-key
  (testing "single key"
    (is (= (build-target-key {:key "created_at"}) "created_at")))
  (testing "ignore invalid key"
    (is (= (build-target-key {:key "some-unknown-key"}) nil))))
