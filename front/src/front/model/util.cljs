(ns front.model.util
  (:refer-clojure :exclude [update])
  (:require [clojure.string :refer [join]]
            [goog.string :refer [format]]
            [re-graph.core :as re-graph]
            [front.model.util.core :as util.core]))

(defn build-select-options-from-list-and-total [list-and-total keys-show]
  (for [item (:list list-and-total)]
    (let [id (:id item)
          value (->> (for [key-show keys-show]
                       (key-show item))
                     (remove nil?) first str)]
      [id value])))

(defn build-str-args-offset-limit-for-index [limit page]
  (util.core/build-query-args-offset-limit-for-index limit page))

(defn build-input-str-for-int [val]
  (util.core/build-query-input-for-int val))

(defn build-input-str-for-str [val]
  (util.core/build-query-input-for-str val))

(defn build-error-messages [errors]
  (for [e errors] (:message e)))

(defn fetch-arr-info-query [arr-info-query]
  (let [query (->> arr-info-query (map :query) (join " ") (format "{ %s }"))]
    #_(println :query query)
    (re-graph/query
     query ()
     (fn [{:keys [data errors]}]
       (let [errors-built (build-error-messages errors)]
         (when-not (empty? errors) (println :errors-for-fetch errors))
         (doseq [{:keys [name-table on-receive]} arr-info-query]
           (when on-receive
             (on-receive ((keyword name-table) data)
                         errors-built))))))))

(defn fetch-info-query [info-query]
  (fetch-arr-info-query [info-query]))

(defn fetch-list-and-total [{:keys [name-table str-keys-of-item on-receive limit page str-params str-additional-field]}]
  (fetch-info-query
   (util.core/build-info-query-fetch-list-and-total
    {:name-table name-table
     :query-keys-of-item str-keys-of-item
     :limit limit
     :page page
     :query-params str-params
     :query-additional-field str-additional-field
     :on-receive on-receive})))

(defn fetch [{:keys [name-table str-keys-of-item on-receive str-params]}]
  (fetch-info-query
   (util.core/build-info-query-fetch
    {:name-table name-table
     :query-params str-params
     :query-keys-of-item str-keys-of-item
     :on-receive on-receive})))

(defn fetch-by-id [{:keys [name-table str-keys-of-item id on-receive str-additional-params]}]
  (fetch-info-query
   (util.core/build-info-query-fetch-by-id
    {:name-table name-table
     :id id
     :query-additional-params str-additional-params
     :query-keys-of-item str-keys-of-item
     :on-receive on-receive})))

(defn mutate-with-receive-params [{:keys [str-field on-receive str-input-params str-keys-receive]}]
  (let [query (format "{ %s( %s ) { errors %s } }"
                      str-field
                      str-input-params
                      (or str-keys-receive ""))]
    #_(println query)
    (re-graph/mutate query () (fn [{:keys [data errors]}]
                                (when-not (empty? errors) (println :errors-for-mutation errors))
                                (on-receive (get data (keyword str-field))
                                            (build-error-messages errors))))))

(defn delete [{:keys [name-table str-input-params on-receive]}]
  (mutate-with-receive-params {:str-field (str name-table "_delete")
                               :on-receive on-receive
                               :str-input-params str-input-params}))

(defn delete-by-id [{:keys [name-table id on-receive]}]
  (delete
   {:name-table name-table :on-receive on-receive
    :str-input-params (format "id: %s" (build-input-str-for-int id))}))

(defn create [{:keys [name-table on-receive str-input-params str-keys-receive]}]
  (mutate-with-receive-params {:str-field (str name-table "_create")
                               :on-receive on-receive
                               :str-input-params str-input-params
                               :str-keys-receive str-keys-receive}))

(defn update [{:keys [name-table on-receive str-input-params str-keys-receive]}]
  (mutate-with-receive-params {:str-field (str name-table "_update")
                               :on-receive on-receive
                               :str-input-params str-input-params
                               :str-keys-receive str-keys-receive}))
