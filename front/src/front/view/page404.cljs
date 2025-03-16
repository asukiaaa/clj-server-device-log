(ns front.view.page404
  (:require
   [front.view.util :as util]
   [front.view.util.label :as util.label]))

(defn core []
  [util/area-content util.label/page-not-found])
