(ns back.test-helper.user
  (:require [back.models.user :as model.user]
            [back.models.util :as model.util]))

(defn create-admin-user []
  (let [params {:email "admin@da.yo"
                :name "ho"
                :password (model.util/build-random-str-alphabets-and-number 10)
                :permission "{\"admin\": \"true\"}"}]
    (model.user/create-with-password params)
    (->> (model.user/get-by-email (:email params))
         (merge params))))
