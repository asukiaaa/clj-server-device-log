(ns front.model.util.authorization-bearer
  (:require [clojure.string :refer [join]]))

(def key-authorization-bearer :authorization_bearer)
(def keys-for-item [key-authorization-bearer])
(def str-keys (join " " (map name keys-for-item)))
