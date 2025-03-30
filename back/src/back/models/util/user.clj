(ns back.models.util.user
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :refer [join]]
   [back.models.util.core :as util.core]))

(s/def ::id integer?)
(s/def ::name string?)
(s/def ::email string?)

(s/def ::user (s/keys :req-un [::id ::name ::email]))

(def name-table "user")
(def key-table (keyword name-table))
(def keys-param [:id :name :email :created_at :updated_at])
(def keys-param-with-permission (conj keys-param :permission))
(defn- get-keys-param [with-permission]
  (if with-permission keys-param-with-permission keys-param))

(defn build-str-keys-select-for-table [& [{:keys [with-permission]}]]
  (->> (for [key (get-keys-param with-permission)]
         (format "%s.%s" name-table (name key)))
       (join ",")))

(defn build-str-select-params-for-joined [& [{:keys [with-permission]}]]
  (util.core/build-str-select-params-for-joined name-table (get-keys-param with-permission)))

(defn build-item-from-selected-params-joined [params & [{:keys [name-table-destination with-permission]}]]
  (util.core/build-item-from-selected-params-joined
   name-table (get-keys-param with-permission)
   params {:name-table-destination name-table-destination}))
