(ns asuki.back.handlers.graphql
  (:require [asuki.back.graphql.resolver :as resolver]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.walmartlabs.lacinia.pedestal2 :as p2]
            [com.walmartlabs.lacinia.pedestal :refer [inject]]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [io.pedestal.interceptor :refer [interceptor]]))

(defn build-schema []
  (-> (io/resource "schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver/resolver-map))
      schema/compile))

(defn ^:private extract-user-info
  [request]
  ;; This is very application-specific ...
  )

(def ^:private user-info-interceptor
  (interceptor
   {:name ::user-info
    :enter (fn [context]
             (let [{:keys [request]} context
                   user-info (extract-user-info request)]
               (assoc-in context [:request :lacinia-app-context :user-info] user-info)))}))

(defn ^:private interceptors
  [schema]
  (-> (p2/default-interceptors schema nil)
      (inject user-info-interceptor :after ::p2/inject-app-context)))

(def core (interceptors build-schema))

#_(defn core [req]
    (let [request-method (:request-method req)]
      (println req)
      (if (= request-method :post)
        (let [body (:json-params req)
              query (:query body)]
          (println "handle post action")
          (println body)
          (println query)
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body (json/write-str (lacinia/execute (build-schema) query nil nil))})
        {:status 200
         :body "graphql"})))
