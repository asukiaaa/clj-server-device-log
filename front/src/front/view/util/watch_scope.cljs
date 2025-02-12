(ns front.view.util.watch-scope
  (:require [clojure.string :refer [split]]
            [front.view.util.label :as util.label]
            [front.view.util :as util]
            [front.model.device :as model.device]))

(defn- build-datetime-from-str-date-and-time [str-date str-time]
  (when-not (empty str-date)
    (if (empty? str-time)
      str-date
      (str str-date " " str-time))))

(defn terms-draft->params [terms]
  (for [[_index term] terms]
    (merge term
           {:datetime_from (build-datetime-from-str-date-and-time
                            (:datetime_from_date term)
                            (:datetime_from_time term))
            :datetime_until (build-datetime-from-str-date-and-time
                             (:datetime_until_date term)
                             (:datetime_until_time term))})))

(defn- parse-str-datetime [str-datetime]
  (when str-datetime
    (split str-datetime #" ")))

(defn- term-params->draft [term]
  (let [[from-date from-time] (parse-str-datetime (:datetime_from term))
        [until-date until-time] (parse-str-datetime (:datetime_until term))]
    (assoc term
           :datetime_from_date from-date
           :datetime_from_time from-time
           :datetime_until_date until-date
           :datetime_until_time until-time)))

(defn terms-params->draft [terms]
  (into {} (for [[index term] (map-indexed vector terms)] [index (term-params->draft term)])))

(defn- render-fields-for-term [state-info-terms index options-for-device-ids on-click-delete]
  (let [term (get (:draft state-info-terms) index)]
    [:div.row
     [:div.col-sm
      [util/render-select util.label/device state-info-terms options-for-device-ids
       {:keys-assoc-in [index :device_id]}]]
     [:div.col-sm
      [util/render-input util.label/from state-info-terms
       {:keys-assoc-in [index :datetime_from_date]
        :type "date"}]
      [util/render-input util.label/time state-info-terms
       {:keys-assoc-in [index :datetime_from_time]
        :disabled (empty? (:datetime_from_date term))
        :type "time"}]]
     [:div.col-sm
      [util/render-input util.label/until state-info-terms
       {:keys-assoc-in [index :datetime_until_date]
        :type "date"}]
      [util/render-input util.label/time state-info-terms
       {:keys-assoc-in [index :datetime_until_time]
        :disabled (empty? (:datetime_until_date term))
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
     (let [options-for-device-ids (model.device/build-select-options-from-list-and-total devices-list-and-total)]
       (for [[index _] (:draft state-info-terms)]
         [:<> {:key index}
          (render-fields-for-term state-info-terms index options-for-device-ids (fn [] (delete-term index)))]))
     [:a.btn.btn-secondary.mt-1 {:href "#" :on-click add-term} util.label/add-term]]))
