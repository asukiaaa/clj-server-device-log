(ns back.test-helper.user
  (:require [clj-time.core :as time]
            [clj-time.format :as time.format]
            [back.models.user :as model.user]
            [back.models.util :as model.util]
            [back.util.time :refer [timeformat-datetime-with-millis]]))

(defn create-admin-user []
  (let [email (str "adimn-" (time.format/unparse timeformat-datetime-with-millis (time/now)) "@da.ze")
        params {:email email
                :name "ho"
                :password (model.util/build-random-str-alphabets-and-number 10)
                :permission "{\"admin\": \"true\"}"}]
    (model.user/create-with-password params)
    (->> (model.user/get-by-email (:email params))
         (merge params))))
