(ns front.route)

(def front "/front")
(def dashboard front)
(def login (str front "/login"))

(def profile (str front "/profile"))
(def users (str front "/users"))
(def user-create (str users "/create"))

(defn user-show [id]
  (str users "/" id))

(defn user-edit [id]
  (str (user-show id) "/edit"))
