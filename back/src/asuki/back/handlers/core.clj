(ns asuki.back.handlers.core
  (:require [clojure.data.json :as json]
            [hiccup.page :refer [html5]]
            #_[hiccup.core :as hc]
            [asuki.back.handlers.util :as handler-util]
            [asuki.back.models.user :as model-user]
            [asuki.back.models.raw-device-log :as model-raw-device-log]))

(defn top [req]
  {:status 200
   ;:headers {"Content-Type" "text/html"}
   :body (html5
          [:head
           [:link {:rel "stylesheet" :href "/public/css/bootstrap.min.css"}]
           #_[:script {:src "./out/main.js" :type "text/javascript"}]
           #_[:base {:href "/"}]
           #_[:script {:src "/front/out/main.js" :type "module"}]]
          [:body
           [:div {:id "app"} "maybe loading js"]
           [:script {:src "/front/out-webpack/main.js" :type "text/javascript"}]
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

(defn device-logs [req]
  (let [logs (model-raw-device-log/get-all)]
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

(defn device-log [req]
  (let [id (:id (:params req))
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
  (let [request-method (:request-method req)]
    (println req)
    (when (and (= request-method :post)
               (handler-util/match-bearer req))
      (let [body (:body req)]
        (println body)
        (model-raw-device-log/create {:data (json/write-str body)}))
      {:status 200
       :body "ok"})))
