(ns asuki.back.graphql.resolver
  (:require [asuki.back.models.raw-device-log :as model-raw-device-log]))

(defn raw-device-logs
  [context args _]
  (println "args in raw-device-logs" args)
  (let [records-and-total (model-raw-device-log/get-records-with-total args)
        logs (:records records-and-total)
        total (:total records-and-total)]
    {:list logs
     :total total}))

(defn game-by-id
  [context args _]
  (println "args in resolve-game-by-id" args)
  {:id (:id args)
   :name "zeyo"})

(defn board-game-designers
  [context args board-game]
  ;; 第三引数には親オブジェクトが渡る
  ;; 略
  )

(defn designer-games
  [context args designer]
  ;; 略
  )

(defn resolver-map []
  {:query/raw-device-logs raw-device-logs
   :query/game-by-id game-by-id
   :BoardGame/designers board-game-designers
   :Designer/games designer-games})
