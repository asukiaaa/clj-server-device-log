(ns front.view.util.breadcrumb
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.util.label :as util.label]))

(defn core [links]
  [:div.ms-2
   (if (empty? links)
     [:span util.label/dashboard]
     [:<>
      [:> router/Link {:to route/dashboard} util.label/dashboard]
      (for [link links]
        (let [path (:path link)
              label (:label link)]
          [:<> {:key (or path label)}
           [:span " > "]
           (if (empty? path)
             [:span label]
             [:> router/Link {:to path} label])]))])])
