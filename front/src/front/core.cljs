(ns front.core
  (:require [reagent.dom :as dom]
            [clojure.string :refer [escape]]
            [goog.string :as string]
            goog.string.format
            ["react" :as react]
            [re-graph.core :as re-graph]))

(re-graph/init {:http {:url "/graphql"
                       :supported-operations #{:query :mutate}}
                :ws nil})

(defn get-by-value-config [data value-config]
  #_(println value-config (vector? value-config))
  (when (not (nil? data))
    (cond
      (string? value-config) (get data value-config)
      (or (vector? value-config) (seq? value-config))
      (let [key (first value-config)
            new-data (get data key)
            new-value-config (rest value-config)]
        #_(println #_data value-config)
        #_(print key new-data new-value-config (type new-value-config))
        (if (= 1 (count value-config))
          new-data
          (get-by-value-config new-data new-value-config)))
      :else data)))

(defn component-device-log [log col-settings]
  (let [[requested-to-open set-requested-to-open] (react/useState false)
        id (:id log)
        data (js->clj (.parse js/JSON (:data log)))
        window-width (. js/window -innerWidth)]
    [:<>
     [:tr
      [:td id]
      (for [col col-settings]
        (let [label (get col "label")
              value-config (get col "value")]
          [:td {:key label} (get-by-value-config data value-config)]))
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

(defn escape-str [text]
  (when-not (nil? text)
    (escape text {\" "\\\""
                  \\ "\\\\"})))

(defn component-device-logs []
  (let [[logs set-logs] (react/useState nil)
        [count-total set-count-total] (react/useState nil)
        on-receive (fn [{:keys [data]}]
                     (set-logs (-> data :raw_device_logs :list))
                     (set-count-total (-> data :raw_device_logs :total)))
        ;; default-str-renderer "[{\"label\": \"camera_id\", \"value\": \"camera_id\"}, {\"label\": \"pi\", \"value\": [\"cpu\", \"model\"]},{\"label\": \"volt\", \"value\":[\"readonly_state\",\"volt_battery\"]}]"
        default-str-renderer "[{\"label\": \"camera_id\", \"value\": \"camera_id\"},{\"label\": \"battery\", \"value\":[\"readonly_state\",\"volt_battery\"]}, {\"label\": \"panel\", \"value\":[\"readonly_state\",\"volt_panel\"]}]"
        [str-renderer set-str-renderer] (react/useState default-str-renderer)
        [str-draft-renderer set-str-draft-renderer] (react/useState default-str-renderer)
        ;; default-str-where "[{\"key\": \"created_at\", \"action\": \"gt\", \"value\": \"2022-03-10 00:00:00\"}]}]"
        default-str-where "[{\"key\": \"created_at\", \"action\": \"in-hours-24\"}]"
        [str-where set-str-where] (react/useState default-str-where)
        [str-draft-where set-str-draft-where] (react/useState default-str-where)
        default-str-order "[{\"key\": \"data\", \"json_key\": \"$.camera_id\", \"dir\": \"desc\"}]"
        [str-order set-str-order] (react/useState default-str-order)
        [str-draft-order set-str-draft-order] (react/useState default-str-order)
        parse-setting #(.parse js/JSON str-renderer)
        parsed-setting (try (parse-setting) (catch js/Error _ nil))
        col-settings (when (not (nil? parsed-setting)) (js->clj parsed-setting))
        parse-error (when (nil? parsed-setting) (try (parse-setting) (catch js/Error e e)))
        update-device-logs
        (fn [str-where str-order]
          (println "update device logs")
          (let [query (goog.string.format "{ raw_device_logs(where: \"%s\", order: \"%s\") { total list { id created_at data } } }"
                                          (escape-str str-where) (escape-str str-order))]
            (println query)
            (re-graph/query query {} on-receive)))
        on-click-apply
        (fn []
          (let [is-different-renderer (not (= str-renderer str-draft-order))
                is-different-where (not (= str-where str-draft-where))
                is-different-order (not (= str-order str-draft-order))]
            (when is-different-renderer (set-str-renderer str-draft-renderer))
            (when is-different-where (set-str-where str-draft-where))
            (when is-different-order (set-str-order str-draft-order))
            (update-device-logs str-draft-where str-draft-order)
            #_(when (or is-different-order is-different-where)
                (update-device-logs str-draft-where str-draft-order))))]
    (react/useEffect
     #(update-device-logs default-str-where default-str-order)
     #())
    [:div
     [:h1 "device logs"]
     [:div (str parse-error)]
     [:form.form-control
      [:div "renderer"]
      [:textarea.form-control.mb-1
       {:type :text :default-value str-renderer
        :on-change (fn [e] (println e) (set-str-draft-renderer (-> e .-target .-value)))}]
      [:div "where"]
      [:textarea.form-control.mb-1
       {:type :text :default-value str-where
        :on-change (fn [e] (println e) (set-str-draft-where (-> e .-target .-value)))}]
      [:div "order"]
      [:textarea.form-control.mb-1
       {:type :text :default-value str-order
        :on-change (fn [e] (println e) (set-str-draft-order (-> e .-target .-value)))}]
      [:a.btn.btn-outline-primary.btn-sm {:on-click on-click-apply} "apply"]]
     [:div.m-1 "renderer: " str-renderer]
     [:div.m-1 "where: " str-where]
     [:div.m-1 "order: " str-order]
     [:div.m-1 "total: " count-total]
     [:table.table.table-sm
      [:thead
       [:tr
        [:th "id"]
        (for [col col-settings]
          (let [label (get col "label")]
            [:th {:key label} label]))
        [:th "created_at"]
        [:th "actions"]]]
      [:tbody
       (for [log logs]
         [:<> {:key (:id log)}
          [:f> component-device-log log col-settings]])]]]))

(defn component-app []
  [:div
   [:f> component-device-logs]])

(dom/render [component-app] (.getElementById js/document "app"))
