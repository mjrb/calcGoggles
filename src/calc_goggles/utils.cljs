(ns calc-goggles.utils
  (:require [cljs.core.async :refer [go <! >! pipe]]
            [calc-goggles.stitch :as s]
            [calc-goggles.browse :as browse]
            [reagent.core :as reagent]))
;;;;;;;;;;;;;; convinience functions ;;;;;;;;;;;;;;;

(defn feild-value [id] (.-value (.getElementById js/document id)))
(defn str-is-float? [string]
  (-> string
      (js/parseFloat)
      (js/isNaN)
      (not)))
(defn draw [] (.  js/modelMaker draw))
(defn make-shape [] (. js/modelMaker makeShape))
(defn str-is-int? [string]
  (some? (re-matches #"^[0-9]+$" string) ))
;;wraps input with a label and br
(defn label
  ([label-text id elem-vec]
  [:span
   [:label {:for id} label-text]
   elem-vec
   [:br]])
  ([label-text]
   [:span
    [:label label-text]
    [:br]]))

;;login-handlers
(defn login [username password app-state]
  (let [[client-chan err-chan] (s/get-client-login (@app-state :api-key) username password)]
    (go
      (let [sclient (<! client-chan)]
        (swap! app-state assoc :client sclient)
        (swap! app-state assoc :content (reagent/as-element [browse/model-browser app-state]))
        (swap! app-state assoc :logged-in true))
      )
  (go (js/alert  (<! err-chan)))))

(defn register [username password api-key]
  (let [[client-chan err-chan] (s/register-email username password api-key)]
    (go (<! client-chan)
        (js/alert "confirmation email sent!"))
    (go (js/alert (<! err-chan)))
    ))

(defn password-reset [email api-key]
  (let [[success-chan err-chan] (s/email-reset-password email api-key)]
    (go (<! success-chan)
        (js/alert "reset link sent!"))
    (go (js/alert (<! err-chan)))))

;;components
(defn register-box [api-key]
  [:div
   (label "username" "uname"
          [:input {:type "text" :id "uname"}])
   (label "password" "pass"
          [:input {:type "password" :id "pass"}])
   [:input {:type "button" :value "register!"
            :on-click #(register (feild-value "uname") (feild-value "pass") api-key)}]
   ])

(defn password-reset-box [api-key]
  [:div
   (label "username" "uname"
          [:input {:type "text" :id "uname"}])
   [:input {:type "button" :value "send password reset"
            :on-click #(password-reset (feild-value "uname") api-key) }]
   ])

(defn login-box [app-state]
   [:div
    (label "username" "uname"
           [:input {:type "text" :id "uname"}])
    (label "password" "pass"
           [:input {:type "password" :id "pass"}])
    [:input {:type "button" :value "login"
             :on-click #(login (feild-value "uname") (feild-value "pass") app-state)
             }]
    [:input
     {:value "forgot password?" :type "button"
      :on-click #(swap! app-state assoc :content [password-reset-box (@app-state :api-key)])
      }]
    ])
