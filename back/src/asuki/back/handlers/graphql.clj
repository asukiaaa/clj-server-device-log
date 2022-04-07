(ns asuki.back.handlers.graphql
  (:require [asuki.back.graphql.resolver :as resolver]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]))

(defn build-schema []
  (-> (io/resource "schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver/resolver-map))
      schema/compile))

(defn core [req]
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
