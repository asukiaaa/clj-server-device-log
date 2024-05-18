(ns front.route)

(def dashboard "/front")
(def login "/front/login")

(defn user-show [id]
  (str "/front/users/" id))

(defn user-edit [id]
  (str (user-show id) "/edit"))
