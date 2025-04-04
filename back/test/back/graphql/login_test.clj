(ns back.graphql.login-test
  (:require
   [clojure.test :as t]
   [back.core]
   [back.test-helper.graphql.core :refer [post-query]]
   [back.test-helper.graphql.login :as graphql.login]
   [back.test-helper.model.user :as h.user]
   [clj-http.cookies :as http.cookies]))

(defn get-id-user-loggedin [data]
  (-> data :user_loggedin :user :id))

(t/deftest test-login
  (h.user/with-user-admin [user-admin]
    #_(println user-admin)
    (t/testing "Able to create admin user"
      #_(println user-admin)
      (t/is (not (nil? (:id user-admin)))))
    (t/testing "Empty user before login"
      (t/is (nil? (graphql.login/get-user-loggedin nil))))
    (t/testing "Able to login"
      (let [query (graphql.login/build-mutation-login (:email user-admin) (:password user-admin))
            cookie-store (http.cookies/cookie-store)
            {:keys [body]} (post-query query {:cookie-store cookie-store})]
        #_(println :query query)
        #_(println :result body)
        (t/is (= (-> body :data :login :email) (:email user-admin)))
        (t/testing "Able to get user_loggedin"
          (let [user-loggedin (graphql.login/get-user-loggedin cookie-store)]
            (t/is (= (:email user-loggedin) (:email user-admin))))))
      (graphql.login/with-loggedin-session [cookie-store user-admin]
        (t/testing "Able to get user_loggedin"
          (let [user-loggedin (graphql.login/get-user-loggedin cookie-store)]
            #_(println :user-loggedin user-loggedin)
            #_(println :user-admin user-admin)
            (t/is (= (:email user-loggedin) (:email user-admin)))))))))

(def tests-to-create
  ["TODO list for create test for graphql"
   "device_logs_for_watch_scope: cannot see logs that the drvice does not belongs to user team by member"
   "files_for_watch_scope: cannot see logs that the drvice does not belongs to user team by member"])

#_(t/run-tests)

#_(defn main [& args]
    (t/run-tests)
    (println "hi on main in test"))
