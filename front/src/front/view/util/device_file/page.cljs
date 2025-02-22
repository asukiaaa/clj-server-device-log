(ns front.view.util.device-file.page
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            ["react-bootstrap" :as bs]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.view.util.label :as util.label]
            [front.util.timezone :as util.timezone]))

(defn render-card [device-file & [{:keys [without-device on-click-image]}]]
  (let [width 200
        height 150
        path-url (or (:path_thumbnail device-file) (:path device-file))
        recorded-at (:recorded_at device-file)
        id-device (:device_id device-file)
        device (:device device-file)]
    [:div.card.m-2 {:style {:float :left :width width}}
     [:a {:href "#" :on-click (fn [e] (.preventDefault e) (when on-click-image (on-click-image device-file)))}
      [:img.card-img-top {:src path-url
                          :style {:object-fit :contain
                                  :width width :height height}}]]
     [:div.card-body.p-1
      (when-not without-device
        [:div
         [:> router/Link {:to (route/device-device-files id-device)} (util.label/device-item device)]])
      [:div (util.timezone/build-datetime-str-in-timezone recorded-at {:datetime-format util.timezone/date-fns-format-with-timezone-until-minutes})]]]))

(defn render-hiddend-image [item]
  (when item
    [:img {:src (:path item)
           :style {:position :absolute
                   :height "5%"
                   :width "5%"
                   ;:visibility :hidden
                   :z-index -1}}]))

(defn render-modal [{:keys [index-show-modal set-index-show-modal items on-click-prev on-click-next]}]
  (let [item-on-modal (get items index-show-modal)
        index-last (dec (count items))
        [map-items-loaded set-map-items-loaded] (react/useState {})
        add-item-to-loaded-if-not
        (fn [index]
          (when-let [item (get items index)]
            (let [item-loaded (get map-items-loaded index)]
              (when (and (seq item)
                         (not (= (:path item) (:path item-loaded))))
                (set-map-items-loaded (assoc map-items-loaded index item))))))]
    (when-not (nil? index-show-modal)
      (add-item-to-loaded-if-not index-show-modal)
      (add-item-to-loaded-if-not (dec index-show-modal))
      (add-item-to-loaded-if-not (inc index-show-modal)))
    [:> bs/Modal {:show (seq item-on-modal) :size :xl :onHide #(set-index-show-modal nil)}
     [:> bs/Modal.Header {:closeButton true}
      (let [device (-> item-on-modal :device)]
        [:<>
         [:> router/Link {:to (route/device-device-files (:id device))} (util.label/device-item device)]
         " "
         (util.timezone/build-datetime-str-in-timezone
          (:recorded_at item-on-modal))])]
     [:> bs/Modal.Body {:class :p-0}
      [:<>
       (for [[key item] map-items-loaded]
         [:<> {:key key}
          (render-hiddend-image item)])]
      [:div.d-flex.justify-content-center
       [:> bs/Button {:on-click on-click-prev :class :m-1 :disabled (= index-show-modal 0)} util.label/prev]
       [:> bs/Button {:on-click on-click-next :class :m-1 :disabled (= index-show-modal index-last)} util.label/next]]
      [:img {:src (:path item-on-modal)
             :key (:path item-on-modal)
             :style {:object-fit :contain
                     :width "100%"}}]]
     [:> bs/Modal.Footer
      #_[:> bs/Button {:on-click #(set-item-in-modal nil)} util.label/close]
      [:> bs/Button {:on-click on-click-prev :disabled (= index-show-modal 0)} util.label/prev]
      [:> bs/Button {:on-click on-click-next :disabled (= index-show-modal index-last)} util.label/next]]]))

(defn core [fetch-list-and-total & [{:keys [on-receive]}]]
  (let [location (router/useLocation)
        [list-and-total set-list-and-total] (react/useState)
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
     (render-modal {:index-show-modal index-show-modal
                    :set-index-show-modal set-index-show-modal
                    :items (:list list-and-total)
                    :on-click-prev on-click-prev
                    :on-click-next on-click-next})
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        element-pagination
        [:div.ms-2 "total " total]
        [:div {:style {:width "100%" :overflow :auto}}
         (for [[index item] (map-indexed vector received-list)]
           [:<> {:key (:path item)}
            (render-card item {:on-click-image (fn [_] (on-click-image index))})])]
        element-pagination]})]))
