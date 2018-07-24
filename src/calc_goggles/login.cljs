(ns calc-goggles.login
  (:require [calc-goggles.browse :as browse]
            [calc-goggles.stitch :as s]
            [reagent.core :as reagent]
            [calc-goggles.utils :as utils :refer [label feild-value]]
            [cljs.core.async :refer [go >! <! pipe]]))
(defn anon-login [app-state]
  (-> (s/get-clientp (@app-state :anon-api-key))
      (.then (fn [client] (.login client) client))
      (.then (fn [client]
               (swap! app-state assoc :client client)
               (swap! app-state assoc :content
                      (reagent/as-element [browse/model-browser app-state]))
               (print (str "got anon client"
                           (.authedId (@app-state :client))))))
      (.catch (fn [err] (js/alert (str "failed to connect to calcGoggles."
                             "please try to refresh page to reconnect. "
                             err))))
               ))

(defn login [username password app-state]
  (let [[client-chan err-chan] (s/get-client-login (@app-state :api-key) username password)]
    (go
      (let [sclient (<! client-chan)]
        (swap! app-state assoc :client sclient)
        (swap! app-state assoc :content (reagent/as-element [browse/model-browser app-state]))
        (swap! app-state assoc :logged-in true))
      )
    (go (js/alert  (<! err-chan)))))

(defn logout [app-state]
  (swap! app-state assoc :content [browse/model-browser])
  (swap! app-state assoc :logged-in false)
  ;;we only pretend to log out, because stitch doesn't really let us
  (swap! app-state (fn [state]
                     (.logout (state :client))
                     (anon-login app-state)
                     (assoc state :content (reagent/as-element [browse/model-browser app-state]))
                     )))

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
  [:div.container
   (label "email" "uname"
          [:input.form-control {:type "text" :id "uname"}])
   (label "password" "pass"
          [:input.form-control {:type "password" :id "pass"}])
   (utils/boot-btn-primary "register!"
            #(register (feild-value "uname") (feild-value "pass") api-key))
   ])

(defn password-reset-box [api-key]
  [:div.container
   (label "email" "uname"
          [:input.form-control {:type "text" :id "uname"}])
   (utils/boot-btn-primary "send password reset"
            #(password-reset (feild-value "uname") api-key))
   ])

(defn login-box [app-state-atom]
   [:div.container
    (label "email" "uname"
           [:input.form-control {:type "text" :id "uname"}])
    (label "password" "pass"
           [:input.form-control {:type "password" :id "pass"}])
    [:span.btn-group
     (utils/boot-btn-primary
      "login"
      #(login (feild-value "uname") (feild-value "pass") app-state-atom))
     (utils/boot-btn-primary
      "forgot password?"
      #(utils/set-content! app-state-atom
                           [password-reset-box (@app-state-atom :api-key)]))
    ]])
