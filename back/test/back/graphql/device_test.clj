(ns back.graphql.device-test
  (:require
   [clojure.test :as t]
   [back.core]
   [back.test-helper.graphql.login :as graphql.login]
   [back.test-helper.graphql.device :as graphql.device]
   [back.test-helper.graphql.util :as graphql.util]
   [back.test-helper.model.device-type :as h.model.device-type]
   [back.test-helper.model.device :as h.model.device]
   [back.test-helper.model.bundled :as h.model.bundled]))

(def key-user-team-device-type-manager :device-type-manager)
(def key-user-team-device-user :device-user)

(defn test-visivility [{:keys [map-user devices map-team]}]
  (t/is (seq map-user))
  (t/is (seq devices))
  (t/is (seq map-team))
  (let [ids-device-all (map :id devices)
        cases-test
        [{:key-watcher [:no-team :admin]
          :ids-device-visible ids-device-all}
         {:key-watcher [:no-team :standalone]
          :ids-device-visible nil}
         {:key-watcher [key-user-team-device-user :owner]
          :ids-device-visible ids-device-all}
         {:key-watcher [key-user-team-device-user :member]
          :ids-device-visible nil}
         {:key-watcher [key-user-team-device-user :member-admin]
          :ids-device-visible ids-device-all}
         {:key-watcher [key-user-team-device-type-manager :owner]
          :ids-device-visible ids-device-all}
         {:key-watcher [key-user-team-device-type-manager :member]
          :ids-device-visible nil}
         {:key-watcher [key-user-team-device-type-manager :member-admin]
          :ids-device-visible ids-device-all}]]
    (doseq [{:keys [key-watcher ids-device-visible]} cases-test]
      (let [user-watcher (get-in map-user key-watcher map-user)]
        (t/is (seq user-watcher))
        (graphql.login/with-loggedin-session [cookie-store user-watcher]
          (let [list-and-total-user (graphql.device/get-list-and-total cookie-store)
                got-devices (:list list-and-total-user)]
            (doseq [device devices]
              (let [id-device (:id device)
                    info-test
                    {:key-watcher key-watcher
                     :key-item (:name device)
                     :item device
                     :list got-devices
                     :get-item-by-id #(graphql.device/get-by-id cookie-store %)}]
                (if (some #(= % id-device) ids-device-visible)
                  (graphql.util/test-visible info-test)
                  (graphql.util/test-invisible info-test))))))))))

(t/deftest core
  (h.model.bundled/with-data-user-no-team [data nil]
    (h.model.bundled/with-data-user-and-team [data {:data-to-merge data
                                                    :key-team key-user-team-device-type-manager}]
      (let [user-team-devie-type-manager (-> data :map-team key-user-team-device-type-manager)]
        (h.model.device-type/with-device-type [device-type [user-team-devie-type-manager]]
          (h.model.bundled/with-data-user-and-team [data {:data-to-merge (assoc data :device-type device-type)
                                                          :key-team key-user-team-device-user}]
            (let [device-owner-user-team (-> data :map-team key-user-team-device-user)]
              (h.model.device/with-devices [devices [device-type device-owner-user-team]]
                (test-visivility (assoc data :devices devices))))))))))
