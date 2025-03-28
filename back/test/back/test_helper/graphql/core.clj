(ns back.test-helper.graphql.core
  (:require
   [cheshire.core :as cheshire]
   [clj-http.client :as http.client]
   [back.config :refer [port]]))

(defn build-url []
  (format "http://localhost:%d/graphql" port))

(defn post-query [query & [options]]
  (let [body (cheshire/generate-string {:query query})]
    #_(println :body-request body)
    (-> (http.client/post (build-url)
                          (merge
                           {:body body
                            :content-type :json
                            :throw-exceptions? false}
                           options))
        ((fn [result]
           (assoc result :body (cheshire/parse-string (:body result) true))))
        #_((fn [item] (println :body-response (:body item)) item)))))
