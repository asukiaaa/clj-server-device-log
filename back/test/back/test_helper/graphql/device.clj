(ns back.test-helper.graphql.device
  (:require
   [back.test-helper.graphql.util :as graphql.util]))

(def keys-item [:id :name :user_team_id :device_type_id])

(defn get-list-and-total [cookie-store]
  (graphql.util/get-list-and-total
   {:key-field :devices
    :keys-item keys-item
    :cookie-store cookie-store}))

(defn get-by-id [cookie-store id]
  (graphql.util/get-by-id
   id
   {:key-field :device
    :keys-item keys-item
    :cookie-store cookie-store}))
