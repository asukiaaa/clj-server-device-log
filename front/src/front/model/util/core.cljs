(ns front.model.util.core
  (:require [clojure.string :refer [escape join]]
            [goog.string :refer [format]]))

(defn build-query-table-and-keys [name-table query-keys & [{:keys [query-keys-additional]}]]
  (format "%s {%s%s}"
          name-table
          query-keys
          (if query-keys-additional (str " " query-keys-additional) "")))

(defn- escape-int [val]
  (when-not (nil? val)
    (if (int? val)
      val
      (js/parseInt val))))

(defn- escape-str [text]
  (when-not (nil? text)
    (escape text {\" "\\\""
                  \\ "\\\\"})))

(defn build-query-input-for-int [val]
  (let [val (escape-int val)]
    (if (nil? val) "null" (str val))))

(defn build-query-input-for-str [val]
  (let [val (escape-str val)]
    (if (nil? val) "null" (format "\"%s\"" val))))

(defn build-info-query-fetch [{:keys [name-table query-params query-keys-of-item on-receive]}]
  {:name-table name-table
   :query (format "%s (%s) { %s }" name-table query-params query-keys-of-item)
   :on-receive on-receive})

(defn build-info-query-fetch-by-id [{:keys [name-table query-keys-of-item id on-receive query-additional-params]}]
  (let [query-params (join ", " (filter seq
                                        [(format "id: %s" (build-query-input-for-int id))
                                         query-additional-params]))]
    (build-info-query-fetch
     {:name-table name-table
      :query-params query-params
      :query-keys-of-item query-keys-of-item
      :on-receive on-receive})))

(defn build-query-args-offset-limit-for-index [limit page]
  (let [limit (if (int? limit) limit (js/parseInt limit))
        page (if (int? page) page (js/parseInt page))]
    (->> [(when (int? limit) (format "limit: %d" limit))
          (when (int? page) (format "page: %d" page))]
         (filter seq)
         (join ", "))))

(defn build-info-query-fetch-list-and-total [{:keys [name-table query-keys-of-item limit page query-params query-additional-field on-receive]}]
  (let [query-offset-limit-for-user (build-query-args-offset-limit-for-index limit page)
        query-params (if query-params (str query-params ", " query-offset-limit-for-user) query-offset-limit-for-user)
        query-args-with-parenthesis (format "(%s)" query-params)
        query (format "%s %s { total list { %s } %s }"
                      name-table
                      query-args-with-parenthesis
                      query-keys-of-item
                      (or query-additional-field ""))]
    {:query query
     :name-table name-table
     :on-receive on-receive}))
