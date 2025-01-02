(ns front.view.util.device-group-api-key.explanation)

(defn permission []
  [:<>
   [:div "variables for permission"]
   [:ul
    [:li
     [:div "create_device"]
     [:div "boolean"]
     [:div "able to create a device from /api/device_group/device"]]]])
