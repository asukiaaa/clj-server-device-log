(ns back.test-helper.user
  (:require
   [java-time.api :as java-time]
   [clojure.spec.alpha :as s]
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

; https://qiita.com/lagenorhynque/items/eebb9a36859789520dbf#10-%E3%83%86%E3%82%B9%E3%83%88

(s/fdef with-admin-user
  :args (s/cat :bindings (s/and (s/coll-of any? :kind vector? :count 1)
                                ::binding)
               :body (s/* any?)))

(defmacro with-admin-user
  {:clj-kondo/lint-as 'clojure.core/fn}
  [bindings & body]
  `(let [~(first bindings) (create-admin-user)]
     (try
       ~@body
       (finally (model.user/delete (:id ~(first bindings)))))))
