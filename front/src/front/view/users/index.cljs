(ns front.view.users.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.users.util :as v.user.util]
            [front.view.util :as util]
            [front.model.user :as model.user]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]))

(defn render-user [user on-delete user-loggedin]
  [:tr
   [:td (:name user)]
   [:td (:email user)]
   [:td
    (util/render-list
     (v.user.util/build-related-links user on-delete user-loggedin)
     (fn [link] [:<> link " "]))]])

(defn-  page []
  (let [location (router/useLocation)
        user-loggedin (util/get-user-loggedin)
        [user-list-and-total set-user-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        users (:list user-list-and-total)
        total (:total user-list-and-total)
        number-result (count users)
        query-params (util/read-query-params)
        number-page (or (:page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        build-url-by-page
        (fn [page] (format "%s?page=%d&limit=%d" route/users page number-limit))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.user/fetch-list-and-total
                     {:limit number-limit
                      :page number-page
                      :on-receive (fn [result errors]
                                    (set-user-list-and-total result)
                                    (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:f> breadcrumb/core [{:label util.label/users}]]
     (when (model.user/admin? user-loggedin)
       [util/area-content
        [:> router/Link {:to route/user-create} util.label/create]])
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [util/area-content
         (util.label/result-in-total number-result total)]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th util.label/name]
           [:th util.label/email]
           [:th util.label/action]]]
         [:tbody
          (for [user users]
            [:<> {:key (:id user)}
             [:f> render-user user load-list user-loggedin]])]]
        [util/area-content
         [:f> pagination/core {:build-url build-url-by-page
                               :total-page number-total-page
                               :current-page number-page}]]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
