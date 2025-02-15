(ns front.view.util.device-file.card
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.util.timezone :as util.timezone]
            [front.view.util.label :as util.label]))

(defn core [device-file & [{:keys [without-device on-click-image]}]]
  (let [width 200
        height 150
        path-url (:path device-file)
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
