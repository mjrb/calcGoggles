(ns calc-goggles.core
  (:require [reagent.core :as reagent :refer [atom]]
            [calc-goggles.create :refer [creator plot]]
            [calc-goggles.view :refer [model-viewer]]
            [calc-goggles.stitch :as s]
            [cljs.core.async :refer [ go >! <! chan alt! alts! pipe]]
            [calc-goggles.utils :as utils :refer [feild-value label]]
            [calc-goggles.login :as login]
            [calc-goggles.browse :refer [model-browser view-object]]
            [calc-goggles.my-shapes :refer [my-shapes-browser]]))

;(enable-console-print!)
;; apikeys here are ok because this will be secured by an authorized orgin in stitch
(defonce app-state (atom {:api-key "calcgoggles-qwpga"
                          :anon-api-key "calcgoggles-anon-nreiw"
                          :content [:span "loading..."]
                          :client #js{}
                          :logged-in false
                          :db-name "test"
                          }))
(def set-content! (partial utils/set-content! app-state))
(login/anon-login app-state)

(defn send-shape [new-shape]
  (swap! app-state assoc :shape new-shape)
  (let [owner-id (.authedId (@app-state :client))
        objects (s/atlas-db-coll (@app-state :client) (@app-state :db-name) "objects")]
    (set! (.-owner_id new-shape) owner-id)
    (-> (.updateOne objects #js{:name (.-name new-shape)
                                :owner_id owner-id}
                    new-shape #js{:upsert true})
        (.then (fn [amount] (.findOne objects new-shape #js{:_id true})))
        (.then (fn [id-object] (view-object (.-_id id-object) app-state)))
        (.catch js/alert)
        )))

(defn wrap-auth-check [func]
  (fn []
    (if (not (.isAuthenticated (:client @app-state)))
      (-> (s/get-clientp (@app-state :anon-api-key))
          (.then (fn [client]
                   (swap! app-state assoc :client client)
                   (.login client)))
          (.then (fn [id]
                   (func)
                   (.catch (fn [err]
                             (js/alert (str "failed to connect to calcGoggles."
                                            "please try to refresh page to reconnect. "
                                            err)))))))
      (func))))

(defn on-js-reload [])

(defn no-auth-buttons []
  [:span.btn-group {:style {:margin-right -4}}
   (utils/boot-btn-secondary "calcGoggles" #())
   (utils/boot-btn-primary
    "browse"
    (wrap-auth-check #(set-content! [model-browser app-state])))])

(defn auth-buttons [logged-in?]
  (if logged-in?
    [:span.btn-group
     (utils/boot-btn-primary "create"
                             #(set-content! [creator send-shape]))
     (utils/boot-btn-primary
      "my shapes"
      (wrap-auth-check #(set-content! [my-shapes-browser app-state])))
     (utils/boot-btn-primary "log out" #(login/logout app-state))]
    ;;else
    [:span.btn-group
     (utils/boot-btn-primary "log in"
                             #(set-content! [login/login-box app-state]))
     (utils/boot-btn-primary
      "register"
      #(set-content!
        [login/register-box (@app-state :api-key)]))]
    ))

(defn app []
  [:div
   [:div.btn-toolbar {:style {:margin-bottom 10}}
    [no-auth-buttons]
    [auth-buttons (@app-state :logged-in)]]
   (@app-state :content)])
(reagent/render-component
 [app]
 (. js/document (getElementById "app")))
