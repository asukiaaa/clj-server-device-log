(ns common.hoge
  (:require [clojure.string :refer [join]]
            #?(:cljs [goog.string :refer [format]]
               :clj [clojure.core :refer [format]])))

(defn hello []
  (format "yo %s" "da"))
