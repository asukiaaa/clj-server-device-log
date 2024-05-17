(ns front.model.user
  (:require goog.string
            [re-graph.core :as re-graph]))

(defn login [{:keys [email password on-receive]}]
  (let [mutation (goog.string.format "{ login(email: \"%s\", password: \"%s\") { id email name } }"
                                     email password)] ;TODO escape
    (re-graph/mutate mutation () (fn [{:keys [data]}]
                                   (on-receive (:login data))))))

(defn logout [{:keys [on-receive]}]
  (re-graph/mutate "{ logout }" () (fn [{:keys [data]}]
                                     (on-receive (:logout data)))))

(defn get-loggedin [{:keys [on-receive]}]
  (let [query (goog.string.format "{ user_loggedin { id email name } }")]
    (re-graph/query query () (fn [{:keys [data]}]
                               (on-receive (:user_loggedin data))))))

(defn fetch-list-and-total [{:keys [on-receive]}]
  (let [query "{ users { total list { id email name } } }"]
    (re-graph/query query () (fn [{:keys [data]}]
                               (on-receive (:users data))))))
