(ns asuki.back.handlers.graphql
  (:require [asuki.back.graphql.resolver :refer [resolver-map]]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [buddy.sign.jwt :as jwt]
            #_[clojure.pprint :refer [pprint]]
            [com.walmartlabs.lacinia.pedestal2 :as p2]
            [com.walmartlabs.lacinia.pedestal :refer [inject]]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.parser :as parser]
            [asuki.back.models.user :as model.user]
            [asuki.back.config :as config]
            [io.pedestal.interceptor :refer [interceptor]]))

(defn build-schema []
  (try
    (let [schema (-> (io/resource "schema.edn")
                     slurp
                     edn/read-string
                     (util/inject-resolvers resolver-map)
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
              (when (not (empty? args-login))
                (do (Thread/sleep 1000) ; wait to take tome for brute force attack
                    (model.user/get-by-email-password (:email args-login) (:password args-login))))
              user-in-session #_(-> context :request :session :user)
              (try
                (jwt/unsign (-> context :request :session :user) config/secret-for-session)
                (catch Exception e (println "catched" (.getMessage e))))
              user-loggedin (if (not (empty? user-loggedin-now))
                              user-loggedin-now
                              (model.user/get-by-id (:id user-in-session)))]
          (cond-> context
            (not (empty? user-loggedin))
            (assoc-in [:request :lacinia-app-context :user-loggedin] user-loggedin)))))
    :leave
    (fn [context]
      (let [session (-> context :request :session (or {}))]
        (if (has-query-of-logout? context)
          (assoc-in context [:response :session] (dissoc session :user))
          (if-let [user (or (-> context :request :lacinia-app-context :user-loggedin))]
            (assoc-in context [:response :session]
                      (assoc session :user
                             (jwt/sign user config/secret-for-session)))
            context))))}))

(defn ^:private interceptors
  [schema]
  (-> (p2/default-interceptors schema nil)
      (inject user-info-interceptor :after ::p2/inject-app-context)))

(def core (interceptors build-schema))
