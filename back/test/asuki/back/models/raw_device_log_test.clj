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

(deftest test-building-where
  (testing "where is nil"
    (is (= (build-query-where {:where nil}) nil)))
  (testing "where multiple"
    (is (= (build-query-where {:where [{"key" "id" "action" "not_null"}
                                       {"key" "id" "action" "gt" "value" "10"}]}))
        "WHERE id IS NOT NULL AND id > 10"))
  (testing "where not exists"
    (is (= (build-query-where {:where [{"not_exists"
                                        [{"action" "="
                                          "key" "data"
                                          "json_key" "machine_id"}
                                         {"action" "lt"
                                          "key" "created_at"}]}]
                               :db-table-key "raw_device_log"
                               :base-table-key "rd"})
          "WHERE NOT EXISTS (SELECT 1 FROM raw_device_log AS rd1 WHERE JSON_VALUE(rd.data,\"$.machine_id\") = JSON_VALUE(rd1.data,\"$.machine_id\") AND rd.created_at < rd1.created_at )"))))
