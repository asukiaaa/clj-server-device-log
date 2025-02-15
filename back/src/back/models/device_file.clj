(ns back.models.device-file
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [back.config :refer [db-spec]]
            [back.models.device :as model.device]
            [back.models.util :as model.util]
            [back.util.filestorage :as util.filestorage]))

(def name-table "device_file")
(def key-table (keyword name-table))

(defn filter-params [params]
  (select-keys params [:name :datetime_dir :device_id]))

(defn create-record [params & [{:keys [transaction]}]]
  (jdbc/insert! (or transaction db-spec) key-table (filter-params params)))

(defn create-file [file-input filename id-device & [{:keys [transaction]}]]
  (let [params (util.filestorage/create-file-for-device file-input filename id-device)]
    (create-record params {:transaction transaction})
    (util.filestorage/build-path-url-for-device params)))

(defn- assign-info-to-item-from-map [item {:keys [map-id-device]}]
  (let [device (get map-id-device (:device_id item))]
    (assoc item :device device)))

(defn- assign-device-to-list [list-files & [{:keys [transaction]}]]
  (let [ids-device (->> list-files (map :device_id) distinct)
        devices (model.device/get-list-by-ids ids-device {:transaction transaction})
        map-id-device (into {} (for [device devices] [(:id device) device]))]
    (->> list-files
         (map #(assign-info-to-item-from-map % {:map-id-device map-id-device})))))

(defn- assign-path-url-to-list [list-files]
  (for [item list-files]
    (if (:path item)
      item
      (assoc item :path (util.filestorage/build-path-url-for-device item)))))

(defn- split-by-params [list-files params]
  (let [{:keys [limit page]} params]
    (->> list-files
         (split-at (* limit page))
         second
         (split-at limit)
         first)))

(defn get-list-with-total-for-device [params id-device & [{:keys [transaction]}]]
  #_(let [{:keys [list total]} (model.util/get-list-with-total-with-building-query
                                name-table params {:str-where (format "device_id = %d" id-device)})]
      {:list (-> list
                 (assign-device-to-list {:transaction transaction})
                 assign-path-url-to-list)
       :total total})
  (let [list-files (->> (util.filestorage/get-path-files-for-device id-device)
                        (map util.filestorage/build-info-map-from-path-file)
                        (sort-by :created_at)
                        reverse)
        total (count list-files)]
    {:list (-> list-files
               (split-by-params params)
               (assign-device-to-list {:transaction transaction}))
     :total total}))

(defn get-path-file-for-user [path-url id-user]
  (let [id-device (util.filestorage/get-id-device-from-path-url path-url)
        device (model.device/get-by-id-for-user id-device id-user)]
    (when device
      (util.filestorage/convert-path-url-to-path-file path-url))))

(defn get-list-with-total-latest-each-device [params sql-ids-user-team & [{:keys [transaction]}]]
  (let [ids-device (->> (jdbc/query (or transaction db-spec) sql-ids-user-team)
                        (map :id))
        arr-info-file
        (->> (for [id-device ids-device]
               (->> (for [path-file (-> id-device
                                        util.filestorage/get-path-files-for-device)]
                      (util.filestorage/build-info-map-from-path-file path-file))
                    (sort-by :created_at) reverse first))
             (remove nil?))
        total (count arr-info-file)]
    {:total total
     :list (assign-device-to-list arr-info-file {:transaction transaction})}))
