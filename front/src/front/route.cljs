(ns front.route)

(def dashboard "/front")
(def login "/front/login")

(def users "/front/users")
(def user-create (str users "/create"))

(defn user-show [id]
  (str users "/" id))

(defn user-edit [id]
  (str (user-show id) "/edit"))
