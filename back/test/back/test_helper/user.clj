(ns back.test-helper.user
  (:require
   [cheshire.core :as cheshire]
   [clojure.spec.alpha :as s]
   [back.models.user :as model.user]
   [back.models.util :as model.util]))

(defn- build-params-user-base [{:keys [name-prefix permission]}]
  (let [str-random (model.util/build-random-str-alphabets-and-number 10)
        name (str name-prefix "-" str-random)]
    {:email (str name "@da.ze")
     :name name
     :password (model.util/build-random-str-alphabets-and-number 10)
     :permission (when permission (cheshire/generate-string permission))}))

(defn build-params-user-admin []
  (build-params-user-base {:name-prefix "admin" :permission {:role :admin}}))

(defn build-params-user []
  (build-params-user-base {:name-prefix "normal"}))

(defn create-user-for-params [params]
  (model.user/create-with-password params)
  (->> (model.user/get-by-email (:email params))
       (merge params)))

(s/fdef with-user-params
  :args (s/cat :binding (s/and (s/coll-of any? :kind vector? :count 2)
                               (s/cat :user any? :params any?))
               :body (s/* any?)))

(defmacro with-user-params
  {:clj-kondo/lint-as 'clojure.core/fn}
  [[user params] & body]
  `(let [~user (create-user-for-params ~params)]
     (try
       ~@body
       (finally (model.user/delete (:id ~user))))))

(s/fdef with-admin-user
  :args (s/cat :binding (s/and (s/coll-of any? :kind vector? :count 1)
                               (s/cat :user-admin any?))
               :body (s/* any?)))

(defmacro with-user-admin
  {:clj-kondo/lint-as 'clojure.core/fn}
  [[user-admin] & body]
  `(with-user-params [~user-admin (build-params-user-admin)]
     ~@body))

(s/fdef with-user
  :args (s/cat :binding (s/and (s/coll-of any? :kind vector? :count 1)
                               (s/cat :user-admin any?))
               :body (s/* any?)))

(defmacro with-user
  {:clj-kondo/lint-as 'clojure.core/fn}
  [[user] & body]
  `(with-user-params [~user (build-params-user)]
     ~@body))
