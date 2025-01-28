(ns front.view.util.device-file.card
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.util.label :as util.label]))

(defn core [device-file & [{:keys [without-device]}]]
  (let [width 200
        height 150
        path-url (:path device-file)
        created-at (:created_at device-file)
        id-device (:device_id device-file)
        device (:device device-file)]
    [:div.card.m-2 {:style {:float :left :width width}}
     [:img.card-img-top {:src path-url
                         :style {:object-fit :contain
                                 :width width :height height}}]
     [:div.card-body.p-1
      (when-not without-device
        [:div
         [:> router/Link {:to (route/device-device-files id-device)} (util.label/device-item device)]])
      [:div created-at]]]))
