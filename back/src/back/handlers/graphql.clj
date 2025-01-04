(ns back.handlers.graphql
  (:require [back.graphql.resolver :refer [resolver-map]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.walmartlabs.lacinia.pedestal2 :as p2]
            [com.walmartlabs.lacinia.pedestal :refer [inject]]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as lacinia.util]
            [back.models.user :as model.user]
            [back.handlers.util :as handler.util]
            [io.pedestal.interceptor :refer [interceptor]]))

(defn build-schema []
  (try
    (let [schema (-> (io/resource "schema.edn")
                     slurp
                     edn/read-string
                     (lacinia.util/inject-resolvers resolver-map)
                     schema/compile)]
      schema)
    (catch Exception e
      (println "failed build-schema")
      (println e)
      (throw e))))

(defonce schema (build-schema)) ; check able to build schema on loading code

(defn ^:private extract-user-info [request]
  {:email "todo on extart-user-info"})

(defn get-parsed-query-from-context [context]
  (-> context
      :request
      :lacinia-app-context
      :request
      :parsed-lacinia-query))

(defn get-queries-for [context key]
  (->> (get-parsed-query-from-context context)
       :selections
       (filter #(= key (:field-name %)))))

(defn has-query-of-logout? [context]
  (-> (get-queries-for context :logout)
      empty?
      not))

(defn get-args-for-login [context]
  (-> (get-queries-for context :login)
      first
      :arguments))

(def ^:private user-info-interceptor
  (interceptor
   {:name ::user-info
    :enter
    (fn [context]
      (if (has-query-of-logout? context)
        context
        (let [args-login (get-args-for-login context)
              user-loggedin-now
              (when-not (empty? args-login)
                (Thread/sleep 1000) ; wait to take tome for brute force attack
                (model.user/get-by-email-password (:email args-login) (:password args-login)))
              user-loggedin (or user-loggedin-now
                                (handler.util/decode-and-find-user-in-session
                                 (-> context :request :session :user)))]
          (cond-> context
            (not (empty? user-loggedin))
            (assoc-in [:request :lacinia-app-context :user-loggedin] user-loggedin)))))
    :leave
    (fn [context]
      (let [session (-> context :request :session (or {}))]
        (if (has-query-of-logout? context)
          (assoc-in context [:response :session] (dissoc session :user))
          (if-let [user (-> context :request :lacinia-app-context :user-loggedin)]
            (assoc-in context [:response :session]
                      (assoc session :user
                             (handler.util/encode-user-for-session user)))
            context))))}))

(defn ^:private interceptors
  [build-schema]
  (-> (p2/default-interceptors build-schema nil)
      (inject user-info-interceptor :after ::p2/inject-app-context)))

(def core (interceptors build-schema))
