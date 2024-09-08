(ns front.model.util
  (:refer-clojure :exclude [update])
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

(defn mutate-with-receive-params [{:keys [str-key-request on-receive str-input-params str-keys-receive]}]
  (let [query (goog.string.format "{ %s( %s ) { errors %s } }"
                                  str-key-request
                                  str-input-params
                                  (or str-keys-receive ""))]
    (re-graph/mutate query () (fn [{:keys [data errors]}]
                                (on-receive (get data (keyword str-key-request))
                                            (build-error-messages errors))))))

(defn delete-by-id [{:keys [name-table id on-receive]}]
  (mutate-with-receive-params {:str-key-request (str name-table "_delete")
                               :on-receive on-receive
                               :str-input-params (goog.string.format "id: %d"
                                                                     (escape-int id))}))

(defn create [{:keys [name-table on-receive str-input-params str-keys-receive]}]
  (mutate-with-receive-params {:str-key-request (str name-table "_create")
                               :on-receive on-receive
                               :str-input-params str-input-params
                               :str-keys-receive str-keys-receive}))

(defn update [{:keys [name-table on-receive str-input-params str-keys-receive]}]
  (mutate-with-receive-params {:str-key-request (str name-table "_update")
                               :on-receive on-receive
                               :str-input-params str-input-params
                               :str-keys-receive str-keys-receive}))
