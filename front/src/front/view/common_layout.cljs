(ns front.view.common-layout)

(defn build-info [fn-useState]
  (let [state-fetching (fn-useState)
        state-errors (fn-useState)]
    {:fetching (first state-fetching)
     :set-fetching (second state-fetching)
     :errors (first state-errors)
     :set-errors (second state-errors)}))

(defn fetch-start [{:keys [set-fetching]}]
  (set-fetching true))

(defn fetch-finished [{:keys [set-fetching set-errors]} errors]
  (set-fetching false)
  (set-errors errors))

(defn wrapper [{:keys [info renderer]}]
  (let [{:keys [fetching errors]} info]
    (if fetching
      [:div "fetching"]
      [:<>
       (when errors
         (for [e errors]
           [:div.alert.alert-danger.m-1 {:key e} e]))
       renderer])))
