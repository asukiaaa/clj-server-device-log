(ns back.models.util.watch-scope)

(def name-table "watch_scope")
(def key-table (keyword name-table))

(defn build-query-get-id-user-team [id]
  (->> (format "SELECT user_team_id FROM %s WHERE id = %d"
               name-table
               id)
       (format "(%s)")))
