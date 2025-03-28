(ns back.test-helper.user
  (:require [java-time.api :as java-time]
            [back.models.user :as model.user]
            [back.models.util :as model.util]
            [back.util.time :refer [time-format-datetime-with-millis]]))

(defn create-admin-user []
  (let [email (str "adimn-" (java-time/format time-format-datetime-with-millis (java-time/local-date-time)) "@da.ze")
        params {:email email
                :name "ho"
                :password (model.util/build-random-str-alphabets-and-number 10)
                :permission "{\"admin\": \"true\"}"}]
    (model.user/create-with-password params)
    (->> (model.user/get-by-email (:email params))
         (merge params))))
