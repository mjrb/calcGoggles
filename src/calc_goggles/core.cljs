(ns calc-goggles.core
  (:require [reagent.core :as reagent :refer [atom]]
            [calc-goggles.create :refer [creator plot]]
            [calc-goggles.view :refer [model-viewer]]
            [calc-goggles.stitch :as s]
            [cljs.core.async :refer [ go >! <! chan alt! alts! pipe]]
            [calc-goggles.utils :as utils :refer [feild-value label anon-login]]
            [calc-goggles.browse :refer [model-browser view-object]]))

;(enable-console-print!)
;; apikeys here are ok because this will be secured by an authorized orgin in stitch
(defonce app-state (atom {:api-key "calcgoggles-qwpga"
                          :anon-api-key "calcgoggles-anon-nreiw"
                          :content [:span "loading..."]
                          :client #js{}
                          :logged-in false
                          :db-name "test"
                          }))
(anon-login app-state)
(defn logout []
  (swap! app-state assoc :content [model-browser])
  (swap! app-state assoc :logged-in false)
  ;;we only pretend to log out, because stitch doesn't really let us
  (swap! app-state (fn [state]
                     (.logout (state :client))
                     (anon-login app-state)
                     (assoc state :content (reagent/as-element [model-browser app-state]))
                     )))
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

(defn on-js-reload [] )

(defn no-auth-buttons []
  [:span.btn-group {:style {:margin-right -4}}
   [:input.btn.btn-secondary {:value "calcGoggles" :type "button"}]
   [:input.btn.btn-primary
    {:value "browse" :type "button"
     :on-click (fn []
                 (if (not (.isAuthenticated (:client @app-state)))
                   (anon-login app-state))
                 (swap! app-state assoc :content
                       (reagent/as-element [model-browser app-state])))
     }]]
  )
(defn auth-buttons [logged-in?]
  (if logged-in?
    [:span.btn-group
     [:input.btn.btn-primary
      {:value "create" :type "button"
       :on-click #(swap! app-state assoc :content [creator send-shape])}]
     [:input.btn.btn-primary
      {:value "log out" :type "button"
       :on-click logout }]]
    ;;else
    [:span.btn-group
     [:input.btn.btn-primary
     {:value "log in" :type "button"
      :on-click #(swap! app-state assoc :content
                         (reagent/as-element [utils/login-box app-state]))
                  ;;(print @app-state)
                  }]
     [:input.btn.btn-primary
      {:value "register" :type "button"
       :on-click #(swap! app-state assoc :content
                         [utils/register-box (@app-state :api-key)])}]
     ]
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
