(ns back.test-helper.graphql.user
  (:require
   [back.test-helper.graphql.util :as graphql.util]))

(def keys-item [:id :name :email])

(defn get-list-and-total [cookie-store]
  (graphql.util/get-list-and-total
   {:key-field :users
    :keys-item keys-item
    :cookie-store cookie-store}))

(defn get-by-id [cookie-store id]
  (graphql.util/get-by-id
   id
   {:key-field :user
    :keys-item keys-item
    :cookie-store cookie-store}))
