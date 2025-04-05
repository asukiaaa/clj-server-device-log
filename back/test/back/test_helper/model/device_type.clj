(ns back.test-helper.model.device-type
  (:require
   [back.models.device-type :as model.device-type]))

(defmacro with-device-type
  {:clj-kondo/lint-as 'clojure.core/let}
  [[device-type [owner-user-team & options]] & body]
  `(let [~device-type (model.device-type/create {:name (or (:name ~options)
                                                           (str "type for " (:name ~owner-user-team)))
                                                 :manager_user_team_id (:id ~owner-user-team)})]
     (try
       ~@body
       (finally
         (model.device-type/delete (:id ~device-type))))))
