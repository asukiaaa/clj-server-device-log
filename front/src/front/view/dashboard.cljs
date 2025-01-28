(ns front.view.dashboard
  (:require ["react" :as react]
            ["react-bootstrap" :as bs]
            ["react-router-dom" :as router]
            [front.model.device-file :as model.device-file]
            [front.model.user :as model.user]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.device-file.card :as file.card]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn page []
  (let [user-loggedin (util/get-user-loggedin)
        is-admin (model.user/admin? user-loggedin)
        location (router/useLocation)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        query-params (util/read-query-params)
        number-page (or (:page query-params) 0)
        number-limit (or (:limit query-params) 30)
        [files-list-and-total set-files-list-and-total] (react/useState)
        load-list
        (fn []
          (wrapper.fetching/start info-wrapper-fetching)
          (model.device-file/fetch-list-and-total-latest-each-device
           {:limit number-limit
            :page number-page
            :on-receive (fn [result errors]
                          (set-files-list-and-total result)
                          (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:f> breadcrumb/core []]
     [:> bs/Container {:fluid true}
      [:> bs/Row
       [:> bs/Col {:sm 2 :class :px-0}
        [:div.list-group
         (for [[name url] (remove nil? [(when is-admin [util.label/users route/users])
                                        [util.label/user-teams route/user-teams]
                                        [util.label/watch-scopes route/watch-scopes]
                                        [util.label/device-types route/device-types]
                                        [util.label/devices route/devices]
                                        [util.label/profile route/profile]])]
           [:<> {:key name}
            [:> router/Link {:to url :class "list-group-item list-group-item-action"} name]])]]
       [:> bs/Col {:class :px-0}
        (wrapper.fetching/wrapper
         {:info info-wrapper-fetching
          :renderer
          [:<>
           (let [files (:list files-list-and-total)]
             (if (empty? files)
               [:div util.label/no-file-to-show]
               (for [file files]
                 [:<> {:key (:path file)}
                  [:f> file.card/core file]])))]})]]]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
