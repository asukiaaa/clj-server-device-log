(ns front.view.dashboard
  (:require ["react-bootstrap" :as bs]
            ["react-router-dom" :as router]
            [front.model.device-file :as model.device-file]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.links :as util.links]
            [front.view.util.device-file.page :as file.page]
            [front.view.util :as util]))

(defn page []
  (let [user-loggedin (util/get-user-loggedin)]
    [:<>
     [:> bs/Container {:fluid true}
      [:> bs/Row
       [:> bs/Col {:sm 2 :class :px-0}
        [:f> breadcrumb/core []]
        [:div.list-group
         (for [[name url] (util.links/build-list-menu-links-for-user user-loggedin)]
           [:<> {:key name}
            [:> router/Link {:to url :class "list-group-item list-group-item-action"} name]])]]
       [:> bs/Col {:class :px-0}
        [:f> file.page/core model.device-file/fetch-list-and-total-latest-each-device]]]]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
