(ns front.model.util
  (:require [clojure.string :refer [escape]]
            [goog.string :refer [format]]
            [re-graph.core :as re-graph]))

(defn build-str-args-offset-limit-for-index [limit page]
  #_(println :build-args :limit limit (int? limit) :page page (int? page))
  (let [limit (if (int? limit) limit (js/parseInt limit))
        page (if (int? page) page (js/parseInt page))]
    (->> [(when (int? limit) (format "limit: %d" limit))
          (when  (int? page) (format "page: %d" page))]
         (filter  seq)
         (clojure.string/join  ", "))))

(defn escape-int [val]
  (when-not (nil? val)
    (if (int? val)
      val
      (js/parseInt val))))

(defn escape-str [text]
  (when-not (nil? text)
    (escape text {\" "\\\""
                  \\ "\\\\"})))

(defn build-error-messages [errors]
  (for [e errors] (:message e)))

(defn fetch-list-and-total [{:keys [name-table str-keys-of-list on-receive limit page]}]
  (let [str-offset-limit-for-user (build-str-args-offset-limit-for-index limit page)
        str-args-with-parenthesis (if (empty? str-offset-limit-for-user) ""
                                      (format "(%s)" str-offset-limit-for-user))
        query (format "{ %s %s { total list { %s } } }" name-table str-args-with-parenthesis str-keys-of-list)]
    #_(println :query query)
    (re-graph/query query () (fn [{:keys [data errors]}]
                               (on-receive (get data (keyword name-table))
                                           (build-error-messages errors))))))

(defn fetch-by-id [{:keys [name-table str-keys-of-list id on-receive]}]
  (let [query (goog.string.format "{ %s (id: %d) { %s } }" name-table (escape-int id) str-keys-of-list)]
    (re-graph/query query () (fn [{:keys [data errors]}]
                               (on-receive (get data (keyword name-table))
                                           (build-error-messages errors))))))

(defn delete-by-id [{:keys [name-table id on-receive]}]
  (let [key-request (str name-table "_delete")
        query (goog.string.format "{ %s (id: %d) }"
                                  key-request (escape-int id))]
    (re-graph/mutate query {} (fn [{:keys [data errors]}]
                                (on-receive (get data (keyword key-request))
                                            (build-error-messages errors))))))

(defn create [{:keys [name-table str-input-params on-receive str-keys-receive]}]
  (let [key-request (str name-table "_create")
        query (goog.string.format "{ %s( %s ) { errors %s } }"
                                  key-request
                                  str-input-params
                                  str-keys-receive)]
    (re-graph/mutate query () (fn [{:keys [data errors]}]
                                (on-receive (get data (keyword key-request))
                                            (build-error-messages errors))))))
