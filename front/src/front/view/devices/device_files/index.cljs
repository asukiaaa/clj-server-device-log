(ns front.view.devices.device-files.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.model.device-file :as model.device-file]))

(defn render-device [device-file]
  (let [width 200
        height 150
        path-url (:path device-file)]
    [:div.card.m-2 {:style {:float :left :width width}}
     [:img.card-img-top {:src path-url
                         :style {:object-fit :contain
                                 :width width :height height}}]
     [:div.card-body path-url]]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-device (get params "device_id")
        location (router/useLocation)
        [device set-device] (react/useState)
        [list-and-total set-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        received-list (:list list-and-total)
        total (:total list-and-total)
        query-params (util/read-query-params)
        number-page (or (:page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        build-url-by-page
        (fn [page] (format "%s?page=%d&limit=%d" (route/device-device-files id-device) page number-limit))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.device-file/fetch-list-and-total-for-device
                     {:limit number-limit
                      :page number-page
                      :id-device id-device
                      :on-receive (fn [result errors]
                                    (set-list-and-total result)
                                    (set-device (:device result))
                                    (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/devices :path route/devices}
       {:label (util.label/device device) :path (route/device-show id-device)}
       {:label util.label/files}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:div {:style {:width "100%" :overflow :auto}}
         (for [item received-list]
           [:<> {:key (:path item)}
            [:f> render-device item]])]
        [:f> pagination/core {:build-url build-url-by-page
                              :total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
