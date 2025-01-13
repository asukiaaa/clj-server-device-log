(ns front.view.common.component.pagination
  (:require ["react-router-dom" :as router]))

(def key-page :page)
(def str-key-page (name key-page))
(def num-pages-show-from-center 3)

(defn calc-total-page [limit total]
  (-> (/ total limit)
      Math/ceil
      int))

(defn core [{:keys [total-page]}]
  (let [_ (router/useLocation) ; reload by refreshing page like F5 key
        url-object (new js/URL js/window.location.href)
        search-params (.-searchParams url-object)
        current-page (int (or (.get search-params str-key-page) 0))
        build-path-for-page
        (fn [page]
          (-> url-object .-searchParams (.set str-key-page page))
          (str (.-pathname url-object) (.-search url-object)))]
    [:nav {:aria-label "Page navigation"}
     (when (< 0 total-page)
       [:ul.pagination
        (let [pages (if (< total-page 7)
                      (range total-page)
                      (let [page-block-left (- current-page num-pages-show-from-center)
                            page-block-left (if (>= 0 (- page-block-left 2))
                                              0 page-block-left)
                            page-block-right (+ current-page num-pages-show-from-center 1)
                            page-block-right (if (>= (+ page-block-right 2) total-page)
                                               total-page page-block-right)]
                        (concat (when-not (= page-block-left 0) [0 :dots-left])
                                (range page-block-left page-block-right)
                                (when-not (= page-block-right total-page) [:dots-right (dec total-page)]))))]
          (for [i pages]
            (let [is-link (int? i)]
              [:li.page-item {:key (str "page" i)
                              :class (cond
                                       (not is-link) "disabled"
                                       (= i (int current-page)) "active"
                                       :else "")}
               (if is-link
                 [:> router/Link {:to (build-path-for-page i) :class :page-link} (inc i)]
                 [:span.page-link "..."])])))])]))


