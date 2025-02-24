(ns front.view.util.watch-scope
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [clojure.string :refer [split join]]
            [front.view.util.label :as util.label]
            [front.view.util :as util]
            [front.util.timezone :as util.timezone]
            [front.model.device :as model.device]))

(def key-str-timezone :str-timezone)
(def key-datetime-from :datetime_from)
(def key-datetime-from-date :datetime_from_date)
(def key-datetime-from-time :datetime_from_time)
(def key-datetime-until :datetime_until)
(def key-datetime-until-date :datetime_until_date)
(def key-datetime-until-time :datetime_until_time)

(defn render-term [term]
  (let [device (:device term)]
    [:div
     [:> router/Link {:to (route/device-show (:id device))} (util.label/device-item device)]
     " "
     (let [datetime-from (key-datetime-from term)
           datetime-until (key-datetime-until term)]
       (if (and (nil? datetime-from) (nil? datetime-until))
         util.label/no-term
         (->> [(when datetime-from
                 (util.label/datetime-from-item
                  (util.timezone/build-datetime-str-in-timezone
                   datetime-from
                   {:datetime-format util.timezone/date-fns-format-with-timezone-until-minute})))
               (when datetime-until
                 (util.label/datetime-until-item
                  (util.timezone/build-datetime-str-in-timezone
                   datetime-until
                   {:datetime-format util.timezone/date-fns-format-with-timezone-until-minute})))]
              (remove nil?)
              (join " "))))]))

(defn render-terms [terms]
  (for [term terms]
    [:<> {:key (:id term)}
     (render-term term)]))

(defn- str-date-and-time->str-datetime-in-utc [str-date str-time str-timezone]
  (when-not (empty? str-date)
    (let [str-time (or str-time "00:00")]
      (-> (str str-date " " str-time)
          (util.timezone/datetime-str-without-timzone->datetime-in-timezone {:str-timezone str-timezone})
          (util.timezone/datetime->str-in-timezone
           {:str-timezone util.timezone/timezone-utc
            :datetime-format util.timezone/date-fns-format})))))

(defn terms-draft->params [terms]
  (let [str-timezone (get terms key-str-timezone)]
    (for [[_index term] (dissoc terms key-str-timezone)]
      (merge term
             {key-datetime-from (str-date-and-time->str-datetime-in-utc
                                 (key-datetime-from-date term)
                                 (key-datetime-from-time term)
                                 str-timezone)
              key-datetime-until (str-date-and-time->str-datetime-in-utc
                                  (key-datetime-until-date term)
                                  (key-datetime-until-time term)
                                  str-timezone)}))))

(defn- parse-str-datetime [str-datetime str-timezone]
  (when str-datetime
    (-> str-datetime
        (util.timezone/build-datetime-str-in-timezone {:str-timezone str-timezone})
        (split  #" "))))

(defn- term-params->draft [term str-timezone]
  (let [[from-date from-time] (parse-str-datetime (key-datetime-from term) str-timezone)
        [until-date until-time] (parse-str-datetime (key-datetime-until term) str-timezone)]
    (assoc term
           key-datetime-from-date from-date
           key-datetime-from-time from-time
           key-datetime-until-date until-date
           key-datetime-until-time until-time)))

(defn build-initial-terms-params []
  {key-str-timezone (util.timezone/get)})

(defn terms-params->draft [terms & [{:keys [str-timezone]}]]
  (let [str-timezone (or str-timezone (util.timezone/get))]
    (into {key-str-timezone str-timezone}
          (for [[index term] (map-indexed vector terms)]
            [index (term-params->draft term str-timezone)]))))

(defn- change-timezone-for-str-date-and-time [item key-date key-time str-timezone-old str-timezone-new]
  (let [str-date-old (key-date item)
        str-time-old (key-time item)]
    (if (empty? str-time-old)
      item
      (let [str-datetime (str-date-and-time->str-datetime-in-utc str-date-old str-time-old str-timezone-old)
            [str-date str-time] (parse-str-datetime str-datetime str-timezone-new)]
        (cond-> item
          (seq str-date-old) (assoc key-date str-date)
          (seq str-time-old) (assoc key-time str-time))))))

(defn- change-timezone-for-draft-item [item str-timezone-old str-timezone-new]
  (reduce (fn [item [key-date key-time]]
            (change-timezone-for-str-date-and-time item key-date key-time str-timezone-old str-timezone-new))
          item [[key-datetime-from-date key-datetime-from-time]
                [key-datetime-until-date key-datetime-until-time]]))

(defn change-timezone [str-timezone-new {:keys [draft set-draft]}]
  (let [str-timezone-old (get draft key-str-timezone)
        draft-updated
        (reduce (fn [result key-item]
                  (let [item (get result key-item)]
                    (assoc result key-item (change-timezone-for-draft-item item str-timezone-old str-timezone-new))))
                (assoc draft key-str-timezone str-timezone-new)
                (keys (dissoc draft key-str-timezone)))]
    (set-draft draft-updated)))

(defn- render-fields-for-term [state-info-terms index options-for-device-ids on-click-delete]
  (let [term (get (:draft state-info-terms) index)]
    [:div.row
     [:div.col-sm
      [util/render-select util.label/device state-info-terms options-for-device-ids
       {:keys-assoc-in [index :device_id]}]]
     [:div.col-sm
      [util/render-input util.label/from state-info-terms
       {:keys-assoc-in [index key-datetime-from-date]
        :type "date"}]
      [util/render-input util.label/time state-info-terms
       {:keys-assoc-in [index key-datetime-from-time]
        :disabled (empty? (key-datetime-from-date term))
        :type "time"}]]
     [:div.col-sm
      [util/render-input util.label/until state-info-terms
       {:keys-assoc-in [index key-datetime-until-date]
        :type "date"}]
      [util/render-input util.label/time state-info-terms
       {:keys-assoc-in [index key-datetime-until-time]
        :disabled (empty? (key-datetime-until-date term))
        :type "time"}]]
     [:div.col-sm
      [:div util.label/action]
      [:a.btn.btn-secondary {:href "#"
                             :on-click (fn [e]
                                         (.preventDefault e)
                                         (on-click-delete))}
       util.label/delete]]]))

(defn add-empty-map-to-last [terms]
  (let [key-last (-> terms keys last)
        key-new-last (if (nil? key-last) 0 (inc key-last))]
    (assoc terms key-new-last {})))

(defn render-fields-for-terms [state-info-terms devices-list-and-total]
  (let [add-term
        (fn [e]
          (.preventDefault e)
          ((:set-draft state-info-terms) (add-empty-map-to-last (:draft state-info-terms)))
          ((:set-default state-info-terms) (add-empty-map-to-last (:default state-info-terms))))
        delete-term
        (fn [index]
          ((:set-draft state-info-terms) (dissoc (:draft state-info-terms) index))
          ((:set-default state-info-terms) (dissoc (:default state-info-terms) index)))]
    [:div
     [:div util.label/terms]
     [util/render-select util.label/timezone state-info-terms (util.timezone/build-options-for-select)
      {:keys-assoc-in [key-str-timezone]
       :without-empty-option true
       :override-on-change
       (fn [value state-info _keys-assoc-in]
         (change-timezone value state-info))}]
     (let [options-for-device-ids (model.device/build-select-options-from-list-and-total devices-list-and-total)]
       (for [[index _] (dissoc (:draft state-info-terms) key-str-timezone)]
         [:<> {:key index}
          (render-fields-for-term state-info-terms index options-for-device-ids (fn [] (delete-term index)))]))
     [:a.btn.btn-secondary.mt-1 {:href "#" :on-click add-term} util.label/add-term]]))
