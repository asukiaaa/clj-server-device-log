(ns front.model.user
  (:require goog.string
            [re-graph.core :as re-graph]))

(defn login [{:keys [email password on-receive]}]
  (let [mutation (goog.string.format "{ login(email: \"%s\", password: \"%s\") { id email name } }"
                                     email password)]
    (re-graph/mutate mutation () (fn [{:keys [data]}]
                                   (on-receive (:login data))))))
