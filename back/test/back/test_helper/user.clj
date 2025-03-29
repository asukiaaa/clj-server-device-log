(ns back.test-helper.user
  (:require
   [cheshire.core :as cheshire]
   [clojure.spec.alpha :as s]
   [java-time.api :as java-time]
   [back.models.user :as model.user]
   [back.models.util :as model.util]
   [back.util.time :refer [time-format-datetime-with-millis]]))

(defn create-admin-user []
  (let [email (str "adimn-" (java-time/format time-format-datetime-with-millis (java-time/local-date-time)) "@da.ze")
        params {:email email
                :name "ho"
                :password (model.util/build-random-str-alphabets-and-number 10)
                :permission (cheshire/generate-string {:role :admin})}]
    (model.user/create-with-password params)
    (->> (model.user/get-by-email (:email params))
         (merge params))))

(s/fdef with-admin-user
  :args (s/cat :binding (s/and (s/coll-of any? :kind vector? :count 1)
                               (s/cat :user-admin any?))
               :body (s/* any?)))

(defmacro with-admin-user
  {:clj-kondo/lint-as 'clojure.core/fn}
  [[user-admin] & body]
  `(let [~user-admin (create-admin-user)]
     (try
       ~@body
       (finally (model.user/delete (:id ~user-admin))))))
