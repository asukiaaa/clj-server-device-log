(ns back.handlers.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [clojure.walk :refer [keywordize-keys]]
            [hiccup.page :refer [html5]]
            [back.config :as config :refer [db-spec]]
            [back.handlers.util :as handler.util]
            [back.models.device-log :as model.device-log]
            [back.models.device-type-api-key :as model.device-type-api-key]
            [back.models.device :as model.device]
            [back.models.device-file :as model.device-file]
            [back.models.util :as model.util]
            [back.models.user-team :as model.user-team]))

(defn top [req]
  {:status 200
   :body (html5
          [:head
           [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
           [:link {:rel "stylesheet" :href "/css/bootstrap.min.css"}]
           [:title "device log"]
           #_[:script {:src "./out/main.js" :type "text/javascript"}]
           #_[:base {:href "/"}]
           #_[:script {:src "/front/out/main.js" :type "module"}]]
          [:body
           [:div {:id "app"} "loading.."]
           [:script {:src "/out-cljs/main.js" :type "text/javascript"}]
           #_[:script {:src "/front/out/index.js" :type "module"}]
           #_[:base {:href "/front/target/public/"}]
           #_[:script {:src "cljs-out/figwheel-dev-main.js" :type "text/javascript"}]
           #_[:script {:src "./out-webpack/main.js" :type "text/javascript"}]
           #_[:div
              [:h1 "top page"]
              [:p [:a {:href "/users"} "users"]]
              [:p [:a {:href "/device_logs"} "device logs"]]]])})

#_(defn users [req]
    (let [users (model-user/get-all)]
      {:status 200
       :body (html5
              [:div
               [:h1 "users"]
               [:ul
                (for [user users]
                  [:li
                   [:a {:href (str "/users/" (:id user))}
                    (str (:id user) " " (:name user))]])]])}))

#_(defn user [req]
    (let [id (:id (:params req))
          user (model-user/get-by-id id)]
      {:status 200
       :body (html5
              (if user
                [:div
                 [:p (:id user)]
                 [:p (:name user)]]
                [:div "no user"]))}))

#_(defn device-logs [req]
    (let [list-and-total (model-device-log/get-list-with-total)
        ;; total (:total list-and-total)
          logs (:list list-and-total)]
      {:status 200
       :body (html5
              [:div
               [:h1 "device logs"]
               [:table
                [:thead [:tr [:th "id"] [:th "created_at"] [:th "action"]]]
                [:tbody
                 (for [log logs]
                   [:tr
                    [:td (:id log)]
                    [:td (:created_at log)]
                    [:td [:a {:href (str "/device_logs/" (:id log))}
                          "detail"]]])]]])}))

#_(defn device-log [req]
    (let [id (:id (:path-params req))
          log (model-device-log/get-by-id id)]
      {:status 200
       :body (html5
              (if log
                [:div
                 [:p (:id log)]
                 [:p [:pre {:style "overflow:auto"}
                      (-> (:data log) json/read-json json/pprint-json with-out-str)]]
                 [:p (:created_at log)]]
                [:div "no data"]))}))

(defn handle-404 [req]
  {:status 404
   :body (html5
          [:div "404 not found"])})

(defn api-post-device-log-old-auth [req]
  (let [str-bearer (handler.util/get-bearer req)
        matched-bearer (= str-bearer config/key-auth)
        device-to-post (model.device/get-by-key-str str-bearer)]
    (when (or matched-bearer device-to-post)
      (let [body (:json-params req)]
        (model.device-log/create {:data (json/write-str body)
                                  :device_id (:id device-to-post)})
        {:status 200
         :body "ok"}))))

(defn api-post-device-log [req]
  (let [str-bearer (handler.util/get-bearer req)
        device-to-post (model.device/get-by-authorization-bearer str-bearer)]
    (when  device-to-post
      (let [body (:json-params req)
            data (:data body)]
        (model.device-log/create {:data (json/write-str data)
                                  :device_id (:id device-to-post)})
        {:status 200
         :body "ok"}))))

(defn api-post-device [req]
  (jdbc/with-db-transaction [transaction db-spec]
    (let [str-bearer (handler.util/get-bearer req)
          device-type-api-key (model.device-type-api-key/get-by-authorization-bearer str-bearer {:transaction transaction})]
      (when (model.device-type-api-key/has-permission-to-create-device? device-type-api-key)
        (let [params (-> req :json-params json/read-str keywordize-keys)
              id-device-type (:device_type_id device-type-api-key)
              params-device (-> (:device params)
                                (assoc :device_type_id id-device-type))
              result-create (model.device/create params-device {:transaction transaction})
              result-return (if-let [device (model.device/key-table result-create)]
                              {model.util/key-authorization-bearer (model.device/build-authorization-bearer-for-item device)}
                              result-create)]
          {:status 200
           :body (json/write-str result-return)})))))

(defn api-get-device-config [req]
  (let [str-bearer (handler.util/get-bearer req)
        device (model.device/get-by-authorization-bearer str-bearer)]
    (when device
      {:status 200
       :body (-> device :id model.device/get-config json/write-str)})))

(defn api-post-device-file [req]
  (let [str-bearer (handler.util/get-bearer req)
        device (or (model.device/get-by-key-str str-bearer) ; TODO remove after updating key on devices
                   (model.device/get-by-authorization-bearer str-bearer))]
    (when device
      (try
        (let [id-device (:id device)
              info-file (-> req :multipart-params (get "file"))
              info-file-config (-> req :multipart-params (get "file_config"))
              input-file (:tempfile info-file)
              filename (:filename info-file)
              path-file (model.device-file/create-file input-file filename id-device {:file-config-json-str info-file-config})
              result {:file {:path path-file}}]
          {:status 200
           :body (json/write-str result)})
        (catch Exception e
          {:status 500
           :body (str e)})))))

(defn get-file-from-filestorage [request]
  (let [path-url (-> request :path-info)
        id-user (-> request :session :user handler.util/decode-user-in-session :id)
        path-file (model.device-file/get-path-file-for-user path-url id-user)]
    (if path-file
      {:status 200
       :body (io/input-stream path-file)}
      {:status 404})))
