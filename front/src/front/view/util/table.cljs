(ns front.view.util.table
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.view.util.label :as util.label]))

(defn core [fetch-list-and-total labels-header render-item & [{:keys [on-receive]}]]
  (let [location (router/useLocation)
        [list-and-total set-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        received-list (:list list-and-total)
        total (:total list-and-total)
        number-result (count received-list)
        query-params (util/read-query-params)
        number-page (or (pagination/key-page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        load-list
        (fn []
          (wrapper.fetching/start info-wrapper-fetching)
          (fetch-list-and-total
           {:limit number-limit
            :page number-page
            :on-receive (fn [result errors]
                          (set-list-and-total result)
                          (when on-receive (on-receive result errors))
                          (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [util/area-content (util.label/result-in-total number-result total)]
        [:table.table.table-sm
         [:thead
          [:tr
           (for [label labels-header]
             [:th {:key label} label])]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-item item load-list]])]]
        (when-not (= total number-result)
          [util/area-content
           [:f> pagination/core {:total-page number-total-page}]])]})]))
