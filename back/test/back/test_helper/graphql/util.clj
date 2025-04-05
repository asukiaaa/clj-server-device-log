(ns back.test-helper.graphql.util
  (:require
   [clojure.test :as t]
   [venia.core :as v]
   [back.core]
   [back.test-helper.graphql.core :refer [post-query]]))

(defn test-is-in-list [label item list]
  (t/testing label
    (t/is (some #(= (:id %) (:id item)) list))))

(defn test-is-not-in-list [label item list]
  (t/testing label
    (t/is (not (some #(= (:item %) (:id item)) list)))))

(defn test-visible [{:keys [key-watcher key-item item list get-item-by-id]}]
  (test-is-in-list (str key-watcher " can see " key-item " on list") item list)
  (let [item-by-query (get-item-by-id (:id item))]
    (t/testing (str key-watcher " can see " key-item " by get query")
      (t/is (not (nil? item-by-query)))
      (t/is (= (:email item-by-query) (:email item))))))

(defn test-invisible [{:keys [key-watcher key-item item list get-item-by-id]}]
  (t/testing (str "invisible test " key-watcher " cannot see " key-item " should handle with not nil element")
    (t/is (not (nil? (:id item)))))
  (test-is-not-in-list (str key-watcher " cannot see " key-item " on list") item list)
  (let [item-by-query (get-item-by-id (:id item))]
    (t/testing (str key-watcher " cannot see " key-item " by get query")
      (t/is (nil? item-by-query)))))

(defn build-query-get-list-and-total [{:keys [key-field keys-item]}]
  (v/graphql-query {:venia/queries [[key-field {:page 0 :limit 100}
                                     [[:list keys-item] :total]]]}))

(defn build-query-get-by-id [id {:keys [key-field keys-item]}]
  (v/graphql-query {:venia/queries [[key-field {:id id}
                                     keys-item]]}))

(defn get-list-and-total [{:keys [cookie-store key-field] :as options}]
  (let [query (build-query-get-list-and-total options)
        result (post-query query {:cookie-store cookie-store})]
    #_(println result)
    (when-let [errors (-> result :body :errors)]
      (println errors))
    (-> result :body :data key-field)))

(defn get-by-id [id {:keys [cookie-store key-field] :as options}]
  (let [query (build-query-get-by-id id options)
        result (post-query query {:cookie-store cookie-store})]
    #_(println result)
    (when-let [errors (-> result :body :errors)]
      (println errors))
    (-> result :body :data key-field)))
