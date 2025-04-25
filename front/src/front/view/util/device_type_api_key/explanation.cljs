(ns front.view.util.device-type-api-key.explanation)

(defn permission []
  [:<>
   [:div "Variables for permission"]
   [:ul
    [:li
     [:div "create_device"]
     [:div "boolean"]
     [:div "able to create a device from /api/device_type/device"]]
    [:li
     [:div "control_device"]
     [:div "boolean"]
     [:div "able to control device from /ws/device_control/:device_id"]]]])
