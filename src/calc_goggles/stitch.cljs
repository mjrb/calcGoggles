(ns calc-goggles.stitch
  (:require [cljs.core.async :refer [ go >! <! chan pipe]]))
(defn prom->chan
  ([prom err-chan]
   (let [val-chan (chan)]
     (.then prom (fn [val] (go (>! val-chan val))))
     (.catch prom (fn [err] (go (>! err-chan err))))
     val-chan))
  ([prom]
   (let [val-chan (chan)
         err-chan (chan)]
     (.then prom (fn [val] (go (>! val-chan val))))
     (.catch prom (fn [err] (go (>! err-chan err))))
     [val-chan err-chan])))

(defn get-clientp [api-key]
  (-> (.-stitch js/window)
      (.-StitchClientFactory)
      (.create api-key)))
(defn get-client [api-key]
  (-> (get-clientp api-key)
      (prom->chan) ))
(defn login
  ([client user pass err-chan]
  (prom->chan (.login client user pass) err-chan))
  ([client user pass]
  (prom->chan (.login client user pass))))
(defn get-client-login [api-key username password]
  (let [[client-chan err-chan] (get-client api-key)]
    (go
      (let [sclient (<! client-chan)
            login-success (login sclient username password err-chan)]
        (<! login-success)
        (>! client-chan sclient)
        ))
    [client-chan err-chan]
    ))
(defn auth-provider [client type]
  (-> (.-auth client)
      (.provider type)))
(defn email-reset-password [email api-key]
  "sends password reset to email and returns channels for errors an success"
  (let [[client-chan err-chan] (get-client api-key)
        result-chan (chan)]
    (go (let [success-chan (-> (<! client-chan)
                                           (auth-provider "userpass")
                                           (.sendPasswordReset email)
                                           (prom->chan err-chan))]
          ;;(go (js/alert (str "222: " (<! fail-chan))))
          (pipe success-chan result-chan)))
    [result-chan err-chan]))
(defn register-email [username password api-key]
  (let [[client-chan err-chan] (get-client api-key)]
    (go (let [sclient (<! client-chan)
              register-prom (.register sclient username password)
              success-chan (prom->chan register-prom err-chan)]
          (<! success-chan)
          (>! client-chan sclient)
          ))
    [client-chan err-chan]))
