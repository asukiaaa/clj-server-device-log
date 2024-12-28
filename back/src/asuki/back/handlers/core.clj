(ns asuki.back.handlers.core
  (:require [clojure.data.json :as json]
            [hiccup.page :refer [html5]]
            [asuki.back.handlers.util :as handler-util]
            [asuki.back.models.raw-device-log :as model-raw-device-log]
            [asuki.back.models.device :as model.device]))

(defn top [req]
  {:status 200
   :body (html5
          [:head
           [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
           [:link {:rel "stylesheet" :href "/css/bootstrap.min.css"}]
           #_[:script {:src "./out/main.js" :type "text/javascript"}]
           #_[:base {:href "/"}]
           #_[:script {:src "/front/out/main.js" :type "module"}]]
          [:body
           [:div {:id "app"} "maybe loading js"]
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
    (let [list-and-total (model-raw-device-log/get-list-with-total)
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
          log (model-raw-device-log/get-by-id id)]
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

(defn api-raw-device-log [req]
  (let [request-method (:request-method req)
        matched-bearer (handler-util/match-bearer req)
        key-post (-> req :query-params :key_post)
        device-to-post (model.device/get-by-key-post key-post)]
    (when (and (= request-method :post)
               (or matched-bearer device-to-post))
      (let [body (:json-params req)]
        (println body)
        (model-raw-device-log/create {:data (json/write-str body)
                                      :device_id (:id device-to-post)}))
      {:status 200
       :body "ok"})))
