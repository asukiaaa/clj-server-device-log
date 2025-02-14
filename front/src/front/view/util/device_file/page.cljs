(ns front.view.util.device-file.page
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            ["react-bootstrap" :as bs]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util.device-file.card :as file.card]
            [front.view.util :as util]
            [front.view.util.label :as util.label]
            [front.util.timezone :as util.timezone]))

(defn core [fetch-list-and-total & [{:keys [on-receive]}]]
  (let [location (router/useLocation)
        [list-and-total set-list-and-total] (react/useState)
        [item-on-modal set-item-in-modal] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        received-list (:list list-and-total)
        total (:total list-and-total)
        query-params (util/read-query-params)
        number-page (or (:page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        element-pagination
        (when (or (< number-limit total)
                  (> number-page (/ total number-limit)))
          [:f> pagination/core {:total-page number-total-page
                                :current-page number-page}])
        load-list
        (fn []
          (wrapper.fetching/start info-wrapper-fetching)
          (fetch-list-and-total
           {:limit number-limit
            :page number-page
            :on-receive
            (fn [result errors]
              (set-list-and-total result)
              (when on-receive (on-receive result errors))
              (wrapper.fetching/finished info-wrapper-fetching errors))}))
        on-click-image
        (fn [item]
          (set-item-in-modal item))
        on-click-next
        (fn []
          (let [list (:list list-and-total)
                path-current (:path item-on-modal)
                index-next-item (->> (for [[index item] (map-indexed vector list)]
                                       (when (= (:path item) path-current)
                                         (inc index)))
                                     (remove nil?)
                                     first)
                next-item (get list index-next-item)]
            (set-item-in-modal next-item)))
        on-click-prev
        (fn []
          (let [list (:list list-and-total)
                path-current (:path item-on-modal)
                index-prev-item (->> (for [[index item] (map-indexed vector list)]
                                       (when (= (:path item) path-current)
                                         (dec index)))
                                     (remove nil?)
                                     first)
                prev-item (get list index-prev-item)]
            (set-item-in-modal prev-item)))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:> bs/Modal {:show (seq item-on-modal) :size :xl :onHide #(set-item-in-modal nil)}
      [:> bs/Modal.Header {:closeButton true}
       (let [device (-> item-on-modal :device)]
         [:div
          [:> router/Link {:to (route/device-device-files (:id device))} (util.label/device-item device)]
          " "
          (util.timezone/build-datetime-str-in-timezone
           (:created_at item-on-modal)
           {:datetime-format util.timezone/date-fns-format-datetime-until-minutes-with-timezone})])]
      [:> bs/Modal.Body {:class :p-0}
       [:img {:src (:path item-on-modal)
              :key (:path item-on-modal)
              :style {:object-fit :contain
                      :width "100%"}}]]
      [:> bs/Modal.Footer
       #_[:> bs/Button {:on-click #(set-item-in-modal nil)} util.label/close]
       [:> bs/Button {:on-click on-click-prev} util.label/prev]
       [:> bs/Button {:on-click on-click-next} util.label/next]]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        element-pagination
        [:div.ms-2 "total " total]
        [:div {:style {:width "100%" :overflow :auto}}
         (for [item received-list]
           [:<> {:key (:path item)}
            [:f> file.card/core item {:on-click-image on-click-image}]])]
        element-pagination]})]))
