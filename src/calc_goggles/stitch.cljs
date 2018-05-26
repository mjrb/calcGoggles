(ns calc-goggles.stitch
  (:require [cljs.core.async :refer [alt! go >! <! chan timeout put! take!]]))
(defn prom->chan [prom]
  (let [val-chan (chan)
        err-chan (chan)]
    (.then prom (fn [val] (go (>! val-chan val))))
    (.catch prom (fn [err] (go (>! err-chan err))))
    [val-chan err-chan]))

(defn get-clientp [api-key]
  (-> (.-stitch js/window)
      (.-StitchClientFactory)
      (.create api-key)))
(defn get-client [api-key]
  (-> (get-clientp api-key)
      (prom->chan) ))
