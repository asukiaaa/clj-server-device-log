(ns front.view.common.component.pagination
  (:require ["react-router-dom" :as router]))

(def key-page :page)
(def str-key-page (name key-page))

(defn calc-total-page [limit total]
  (-> (/ total limit)
      Math/ceil
      int))

(defn core [{:keys [total-page]}]
  (let [_ (router/useLocation) ; reload by refreshing page like F5 key
        url-object (new js/URL js/window.location.href)
        search-params (.-searchParams url-object)
        current-page (or (.get search-params str-key-page) 0)
        build-path-for-page
        (fn [page]
          (-> url-object .-searchParams (.set str-key-page page))
          (str (.-pathname url-object) (.-search url-object)))]
    [:nav {:aria-label "Page navigation"}
     [:ul.pagination
      (for [i (range total-page)]
        [:li.page-item {:key (str "page" i)
                        :class (if (= i (int current-page)) "active" "")}
         [:> router/Link {:to (build-path-for-page i) :class :page-link} (inc i)]])]]))
