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
        index-last (dec (count (:list list-and-total)))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        received-list (:list list-and-total)
        [index-show-modal set-index-show-modal] (react/useState)
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
        (fn [index]
          (println :click-image index)
          (set-index-show-modal index))
        on-click-next
        (fn []
          (let [list (:list list-and-total)]
            (set-index-show-modal (if (= index-show-modal (dec (count list)))
                                    nil (inc index-show-modal)))))
        on-click-prev
        (fn []
          (let [list (:list list-and-total)]
            (set-index-show-modal (if (> index-show-modal 0)
                                    (dec index-show-modal) nil))))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     (let [item-on-modal (get (:list list-and-total) index-show-modal)]
       [:> bs/Modal {:show (seq item-on-modal) :size :xl :onHide #(set-index-show-modal nil)}
        [:> bs/Modal.Header {:closeButton true}
         (let [device (-> item-on-modal :device)]
           [:div
            [:> router/Link {:to (route/device-device-files (:id device))} (util.label/device-item device)]
            " "
            (util.timezone/build-datetime-str-in-timezone
             (:recorded_at item-on-modal))])]
        [:> bs/Modal.Body {:class :p-0}
         [:img {:src (:path item-on-modal)
                :key (:path item-on-modal)
                :style {:object-fit :contain
                        :width "100%"}}]]
        [:> bs/Modal.Footer
         #_[:> bs/Button {:on-click #(set-item-in-modal nil)} util.label/close]
         [:> bs/Button {:on-click on-click-prev :disabled (= index-show-modal 0)} util.label/prev]
         [:> bs/Button {:on-click on-click-next :disabled (= index-show-modal index-last)} util.label/next]]])
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        element-pagination
        [:div.ms-2 "total " total]
        [:div {:style {:width "100%" :overflow :auto}}
         (for [[index item] (map-indexed vector received-list)]
           [:<> {:key (:path item)}
            [:f> file.card/core item {:on-click-image (fn [_] (on-click-image index))}]])]
        element-pagination]})]))
