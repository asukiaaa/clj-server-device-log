(ns asuki.back.handlers.graphql
  (:require [asuki.back.graphql.resolver :refer [resolver-map]]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [com.walmartlabs.lacinia.pedestal2 :as p2]
            [com.walmartlabs.lacinia.pedestal :refer [inject]]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.parser :as parser]
            [asuki.back.models.user :as model.user]
            [io.pedestal.interceptor :refer [interceptor]]))

(defn build-schema []
  (-> (io/resource "schema.edn")
      slurp
      edn/read-string
      (util/inject-resolvers resolver-map)
      schema/compile))

(defonce schema (build-schema))

(defn ^:private extract-user-info [request]
  {:email "todo on extart-user-info"})

(def ^:private user-info-interceptor
  (interceptor
   {:name ::user-info
    :enter
    (fn [context]
      (let [parsed-query (-> context
                             :request
                             :lacinia-app-context
                             :request
                             :parsed-lacinia-query)
            args (->> parsed-query
                      :selections
                      (filter #(= :login (:field-name %)))
                      first
                      :arguments)]
        (if (empty? args)
          context
          (do (Thread/sleep 1000) ; wait to take tome for brute force attack
              (if-let [user (model.user/get-by-email-password (:email args) (:password args))]
                (assoc-in context [:request :lacinia-app-context :loggedin-user] (model.user/filter-for-session user))
                context)))))
    :leave
    (fn [context]
      (if-let [user (-> context :request :lacinia-app-context :loggedin-user)]
        (let [session (-> context :request :session (or {}))]
          ;; TODO encrypt user data
          (assoc-in context [:response :session] (assoc-in session [:user] user)))
        context))}))

(defn ^:private interceptors
  [schema]
  (-> (p2/default-interceptors schema nil)
      (inject user-info-interceptor :after ::p2/inject-app-context)))

(def core (interceptors build-schema))
