(ns front.view.common.component.pagination
  (:require ["react-router-dom" :as router]))

(defn calc-total-page [limit total]
  (-> (/ total limit)
      Math/ceil
      int))

(defn core [{:keys [build-url total-page current-page]}]
  [:nav {:aria-label "Page navigation"}
   [:ul.pagination
    (for [i (range total-page)]
      [:li.page-item {:key (str "page" i)
                      :class (if (= i (int current-page)) "active" "")}
       [:> router/Link {:to (build-url i) :class :page-link} (inc i)]])]])
