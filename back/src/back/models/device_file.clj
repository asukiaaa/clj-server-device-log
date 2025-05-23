(ns back.models.device-file
  (:refer-clojure :exclude [update])
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :refer [join]]
            [clojure.walk :refer [keywordize-keys]]
            [java-time.api :as java-time]
            [back.config :refer [db-spec]]
            [back.models.device :as model.device]
            [back.models.util :as model.util]
            [back.models.util.device :as util.device]
            [back.models.util.device-file :as util.device-file]
            [back.models.util.watch-scope :as util.watch-scope]
            [back.models.util.watch-scope-term :as util.watch-scope-term]
            [back.util.filestorage :as util.filestorage]
            [back.util.time :refer [time-format-yyyymmdd-hhmmss]]
            [back.models.util.user-team-permission :as util.user-team-permission]))

(def name-table util.device-file/name-table)
(def key-table util.device-file/key-table)

(defn build-query-join []
  (format "INNER JOIN %s ON %s.id = %s.device_id"
          util.device/name-table
          util.device/name-table
          name-table))
(defn build-str-keys-select-with-peripherals []
  (format "%s.*, %s"
          name-table
          (util.device/build-str-select-params-for-joined)))
(defn build-item [item]
  (-> item
      util.device/build-item-from-selected-params-joined))

(defn filter-params [params]
  (select-keys params [:name :datetime_dir :device_id :recorded_at]))

(defn create-record [params & [{:keys [transaction]}]]
  (jdbc/insert! (or transaction db-spec) key-table (filter-params params)))

(defn create-file [file-input filename id-device & [{:keys [transaction file-config-json-str]}]]
  (let [file-config  (when file-config-json-str (-> file-config-json-str json/read-str keywordize-keys))
        params (util.filestorage/create-file-for-device file-input filename id-device)]
    (create-record (merge file-config params) {:transaction transaction})
    (util.filestorage/build-path-url-for-device params)))

(defn- assign-watch-scoopes-to-list [list-file & [{:keys [transaction]}]]
  (when-not (empty? list-file)
    (let [key-id-device-file (keyword (format "%s_id" util.device-file/name-table))
          sql-ids-device-file (->> (map :id list-file) (join ",") (format "(%s)"))
          query (join " " [(format "SELECT %s.id %s , %s.* from %s"
                                   util.device-file/name-table
                                   (name key-id-device-file)
                                   util.watch-scope/name-table
                                   util.watch-scope/name-table)
                           (format "INNER JOIN %s ON %s.id = %s.watch_scope_id"
                                   util.watch-scope-term/name-table
                                   util.watch-scope/name-table
                                   util.watch-scope-term/name-table)
                           (format "INNER JOIN %s ON %s.device_id = %s.device_id AND %s"
                                   util.device-file/name-table
                                   util.device-file/name-table
                                   util.watch-scope-term/name-table
                                   (util.watch-scope-term/build-sql-datetime-is-target
                                    (format "%s.recorded_at" util.device-file/name-table)))
                           (format "WHERE %s.id IN %s"
                                   util.device-file/name-table
                                   sql-ids-device-file)])
          list-watch-scope (jdbc/query (or transaction db-spec) query)
          map-id-device-file-id-watch-scope
          (reduce (fn [result item]
                    (let [id-device-file (key-id-device-file item)
                          id-watch-scope (:id item)
                          map-for-id-device-file (get result id-device-file)]
                      (assoc result id-device-file (assoc map-for-id-device-file id-watch-scope item))))
                  {} list-watch-scope)
          list-device-file
          (for [item list-file]
            (let [list-watch-scope (vals (get map-id-device-file-id-watch-scope (:id item)))]
              (assoc item (-> util.watch-scope/name-table (str "s") keyword) list-watch-scope)))]
      list-device-file)))

(defn- assign-path-url-to-item [item]
  (if (:path item)
    item
    (assoc item :path (util.filestorage/build-path-url-for-device item))))

(defn- assign-path-url-thumbnail-to-item [item]
  (if (or (:path_thumbnail item) (not (util.filestorage/present-thumbnail-for-device? item)))
    item
    (assoc item :path_thumbnail (util.filestorage/build-path-url-thumbnail-for-device item))))

(defn- assign-path-url-to-list [list-files]
  (for [item list-files]
    (-> item
        assign-path-url-to-item
        assign-path-url-thumbnail-to-item)))

(defn- get-list-with-total-base [params & [{:keys [str-before-where str-order] :as optional-params}]]
  (let [{:keys [transaction]} optional-params
        {:keys [list total]}
        (model.util/get-list-with-total-with-building-query
         name-table params
         (assoc optional-params
                :str-keys-select (build-str-keys-select-with-peripherals)
                :str-before-where (->> [str-before-where (build-query-join)] (remove nil?) (join " "))
                :build-item build-item
                :str-order (or str-order
                               (format "%s.recorded_at DESC"
                                       name-table))))]
    {:list (-> list
               (assign-watch-scoopes-to-list {:transaction transaction})
               assign-path-url-to-list)
     :total total}))

(defn get-list-with-total-for-device [params id-device & [{:keys [transaction]}]]
  (get-list-with-total-base params {:transaction transaction
                                    :str-where (format "device_id = %s" id-device)}))

(defn get-path-file-for-user [path-url id-user & [{:keys [transaction]}]]
  (let [id-device (or (util.filestorage/get-id-device-from-path-url path-url)
                      (util.filestorage/get-id-device-from-path-url-thumbnail path-url))
        device (model.device/get-by-id-in-ids-user-team-or-ids-device
                {:id id-device
                 :ids-user-team (util.user-team-permission/build-query-ids-for-user-write id-user)
                 :transaction transaction})]
    (when device
      (util.filestorage/convert-path-url-to-path-file path-url))))

(defn get-list-for-device [id-device & [{:keys [transaction]}]]
  (jdbc/query (or transaction db-spec)
              (format "SELECT * FROM %s WHERE device_id = %d"
                      name-table
                      id-device)))

(defn get-list-with-total-latest-each-device-for-admin [params & [{:keys [sql-ids-device transaction]}]]
  (get-list-with-total-base
   params
   {:transaction transaction
    :str-before-where
    (let [name-table-left-join "df2"]
      (join " " ["INNER JOIN"
                 (->> [(format "SELECT max(recorded_at) max_recorded_at, device_id FROM %s"
                               name-table)
                       (when sql-ids-device (format "WHERE device_id IN %s" sql-ids-device))
                       "GROUP BY device_id"]
                      (remove nil?)
                      (join " ")
                      (format "(%s)"))
                 name-table-left-join
                 (format "ON %s.recorded_at = %s.max_recorded_at AND %s.device_id = %s.device_id"
                         name-table
                         name-table-left-join
                         name-table
                         name-table-left-join)]))
    :str-order (format "%s.name DESC, %s.recorded_at DESC"
                       util.device/name-table
                       name-table)}))

(defn get-list-with-total-latest-each-device [params sql-ids-device & [{:keys [transaction]}]]
  (get-list-with-total-latest-each-device-for-admin params {:sql-ids-device sql-ids-device :transaction transaction}))

(defn get-list-with-total-for-ids [params sql-ids & [{:keys [transaction]}]]
  (get-list-with-total-base
   params
   {:transaction transaction
    :str-where
    (format "%s.id IN %s" name-table sql-ids)}))

(defn update-for-files-on-local []
  (let [ids-device-on-db (->> (jdbc/query db-spec (util.device/build-sql-ids))
                              (map :id))
        ids-device-on-local (util.filestorage/get-ids-device)]
    #_(println :ids-for-device ids-device-on-db ids-device-on-local)
    (doseq [id-device ids-device-on-local]
      (when (some #(= % id-device) ids-device-on-db)
        (let [path-files (util.filestorage/get-path-files-for-device id-device)
              device-files (get-list-for-device id-device)
              map-device-files (into {} (for [item device-files]
                                          [(util.filestorage/build-path-local-for-device-file item)
                                           item]))
              files-to-create-record
              (reduce
               (fn [files path-file]
                 (if (get map-device-files path-file)
                   files
                   (conj files path-file)))
               [] path-files)
              params-multi-insert
              (for [path files-to-create-record]
                (let [path-url (util.filestorage/convert-path-file-to-path-url path)
                      str-created-at (util.filestorage/get-str-created-at-from-path-url path-url)
                      recorded-at (->> (util.filestorage/parse-str-datetime str-created-at)
                                       (java-time/format time-format-yyyymmdd-hhmmss))]
                  {:device_id (util.filestorage/get-id-device-from-path-url path-url)
                   :datetime_dir str-created-at
                   :name (util.filestorage/get-filename-from-path-url path-url)
                   :recorded_at recorded-at}))]
          #_(println :files-to-create-record files-to-create-record)
          (println :id-device id-device :params-multi-insert params-multi-insert)
          #_(println id-device path-files device-files map-device-files)
          (when-not (empty? params-multi-insert)
            (jdbc/insert-multi! db-spec key-table params-multi-insert))
          (doseq [path-file path-files]
            (when (util.filestorage/is-path-image? path-file)
              (when-not (util.filestorage/present-thumbnail-of-file-local? path-file)
                (println "create thumbnail of" path-file)
                (util.filestorage/create-thumbnail path-file)))))))))
