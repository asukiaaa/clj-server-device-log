(ns front.model.util
  (:refer-clojure :exclude [update])
  (:require [clojure.string :refer [escape join]]
            [goog.string :refer [format]]
            [re-graph.core :as re-graph]))

(defn build-select-options-from-list-and-total [list-and-total keys-show]
  (for [item (:list list-and-total)]
    (let [id (:id item)
          value (->> (for [key-show keys-show]
                       (key-show item))
                     (remove nil?) first str)]
      [id value])))

(defn build-str-args-offset-limit-for-index [limit page]
  #_(println :build-args :limit limit (int? limit) :page page (int? page))
  (let [limit (if (int? limit) limit (js/parseInt limit))
        page (if (int? page) page (js/parseInt page))]
    (->> [(when (int? limit) (format "limit: %d" limit))
          (when (int? page) (format "page: %d" page))]
         (filter seq)
         (join ", "))))

(defn- escape-int [val]
  (when-not (nil? val)
    (if (int? val)
      val
      (js/parseInt val))))

(defn- escape-str [text]
  (when-not (nil? text)
    (escape text {\" "\\\""
                  \\ "\\\\"})))

(defn build-input-str-for-int [val]
  (let [val (escape-int val)]
    (if (nil? val) "null" (str val))))

(defn build-input-str-for-str [val]
  (let [val (escape-str val)]
    (if (nil? val) "null" (format "\"%s\"" val))))

(defn build-error-messages [errors]
  (for [e errors] (:message e)))

(defn fetch-list-and-total [{:keys [name-table str-keys-of-item on-receive limit page str-params str-additional-field]}]
  (let [str-offset-limit-for-user (build-str-args-offset-limit-for-index limit page)
        str-params (if str-params (str str-params ", " str-offset-limit-for-user) str-offset-limit-for-user)
        str-args-with-parenthesis (format "(%s)" str-params)
        query (format "{ %s %s { total list { %s } %s } }"
                      name-table
                      str-args-with-parenthesis
                      str-keys-of-item
                      (or str-additional-field ""))]
    #_(println :query query)
    (re-graph/query query () (fn [{:keys [data errors]}]
                               (when-not (empty? errors) (println :errors-for-fetch-list-and-total errors))
                               (on-receive (get data (keyword name-table))
                                           (build-error-messages errors))))))

(defn fetch-by-id [{:keys [name-table str-keys-of-item id on-receive str-additional-params]}]
  (let [str-params (join ", " (filter seq
                                      [(format "id: %s" (build-input-str-for-int id))
                                       str-additional-params]))
        query (format "{ %s (%s) { %s } }" name-table str-params str-keys-of-item)]
    #_(println :query query)
    (re-graph/query query () (fn [{:keys [data errors]}]
                               (when-not (empty? errors) (println :errors-for-fetch-by-id errors))
                               (on-receive (get data (keyword name-table))
                                           (build-error-messages errors))))))

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

(defn delete-by-id [{:keys [name-table id on-receive]}]
  (mutate-with-receive-params {:str-field (str name-table "_delete")
                               :on-receive on-receive
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
