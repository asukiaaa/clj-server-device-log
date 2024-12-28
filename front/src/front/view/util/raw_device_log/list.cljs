(ns front.view.util.raw-device-log.list
  (:require [cljs-time.core :as t]
            [cljs-time.format :as tf]
            ["react" :as react]
            [goog.string]
            [front.model.raw-device-log :as model.log]))

(defn build-badge-item [record badge data id]
  (let [req-when (get badge "when")
        val-key (get req-when "key")
        text (or (get badge "text") "no text")
        val (model.log/get-val-from-record record val-key {:data data})
        action (get req-when "action")
        hours-action (when (string? action)
                       (-> (re-seq #"(in|not-in)-hours-(\d+)$" action)
                           first
                           #_((fn [x] (println "parsing hours-action" x) x))
                           rest))]
    ;; https://github.com/andrewmcveigh/cljs-time
    (when (cond
            (seq hours-action)
            (let [[in-or-not-in str-hours] hours-action
                  record-time (tf/parse (tf/formatter "YYYY-MM-dd HH:mm:ss.0") val)
                  ;; formatter (tf/formatter "YYYY-MM-dd HH:mm:ss")
                  threshold-time (t/minus (t/now) (t/hours (js/parseInt str-hours)))]
              ;; (println (tf/unparse formatter threshold-time) (tf/unparse formatter record-time))
              ;; (println str-hours (js/parseInt str-hours))
              (if (= in-or-not-in "in")
                (or (t/after? record-time threshold-time) (t/equal? record-time threshold-time))
                (t/before? record-time threshold-time))
              #_(println (tf/show-formatters))
              #_(println js/date-fns.addHours)
              #_(println dfns))
            :else false)
      [:span.badge.bg-secondary.mx-1 {:key (str "badge" text id)} text])))

(defn build-badge [record col-setting data id]
  (when-let [raw-badge (get col-setting "badge")]
    (if (map? raw-badge)
      (build-badge-item record raw-badge data id)
      (for [badge raw-badge] (build-badge-item record badge data id)))))

(defn component-device-log [log col-settings]
  (let [[requested-to-open set-requested-to-open] (react/useState false)
        id (:id log)
        data (js->clj (.parse js/JSON (:data log)))
        window-width (. js/window -innerWidth)]
    [:<>
     [:tr
      [:td id]
      (for [col col-settings]
        [:td {:key (model.log/get-label-from-col-config col)}
         (model.log/get-val-from-record log (get col "key") {:data data})
         (build-badge log col data id)])
      [:td (:created_at log)]
      [:td [:a ;; .btn.btn-outline-primary.btn-sm
            {:href "#"
             :on-click (fn [e]
                         (.preventDefault e)
                         (set-requested-to-open (not requested-to-open)))}
            (if requested-to-open "close" "open")]]]
     (when requested-to-open
       [:tr
        [:td {:colSpan (+ 3 (count col-settings))}
         [:pre {:style {:overflow :auto :max-width (- window-width 40)}}
          (.stringify js/JSON (.parse js/JSON (:data log)) nil 2)]]])]))

(defn core [logs col-settings]
  [:table.table.table-sm
   [:thead
    [:tr
     [:th "id"]
     (for [col col-settings]
       (let [label (model.log/get-label-from-col-config col)]
         [:th {:key label} label]))
     [:th "created_at"]
     [:th "actions"]]]
   [:tbody
    (for [log logs]
      [:<> {:key (:id log)}
       [:f> component-device-log log col-settings]])]])
