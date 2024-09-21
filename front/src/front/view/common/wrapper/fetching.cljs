(ns front.view.common.wrapper.fetching
  (:require [front.view.page404 :as page404]))

(defn build-info [fn-useState]
  (let [state-fetching (fn-useState)
        state-errors (fn-useState)]
    {:fetching (first state-fetching)
     :set-fetching (second state-fetching)
     :errors (first state-errors)
     :set-errors (second state-errors)}))

(defn start [{:keys [set-fetching]}]
  (set-fetching true))

(defn set-errors [{:keys [set-errors]} errors]
  (set-errors errors))

(defn finished [{:keys [set-fetching set-errors]} errors]
  (set-fetching false)
  (set-errors errors))

(defn wrapper [{:keys [info renderer show-404]}]
  (let [{:keys [fetching errors]} info]
    (cond
      show-404 page404/core
      fetching [:div "fetching"]
      :else
      [:<>
       (when errors
         (for [e errors]
           [:div.alert.alert-danger.m-1 {:key e} e]))
       renderer])))
