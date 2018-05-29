(ns calc-goggles.core
  (:require [reagent.core :as reagent :refer [atom]]
            [calc-goggles.create :refer [creator plot]]
            [calc-goggles.view :refer [model-viewer]]
            [calc-goggles.stitch :as s]
            [cljs.core.async :refer [ go >! <! chan alt! alts! pipe]]
            [calc-goggles.utils :as utils :refer [feild-value label]]
            [calc-goggles.browse :refer [model-browser]]))

(enable-console-print!)
(defonce app-state (atom {:api-key "calcgoggles-qwpga"
                          :anon-api-key "calcgoggles-anon-nreiw"
                          :content [:span "loading..."]
                          :client #js{}
                          :logged-in false
                          :shape #js{}
                          :db-name "test"
                          }))
;;get initials anonymous client
(let [[client-chan err-chan] (s/get-client (@app-state :anon-api-key))]
  (go (let [client (<! client-chan)]
        (.login client)
        (swap! app-state assoc :client client)
        (swap! app-state assoc :content (reagent/as-element [model-browser app-state]))
        (print (str "got anon client" (.authedId (@app-state :client))))))
  (go (js/alert (str "failed to connect to calcGoggles. please try to refresh page to reconnect. "
                     (<! err-chan)))))

(defn send-shape [new-shape]
  (swap! app-state assoc :shape new-shape)
  (let [owner-id (.authedId (@app-state :client))
        objects (s/atlas-db-coll (@app-state :client) (@app-state :db-name) "objects")]
    (set! (.-owner_id new-shape) owner-id)
    (-> (.updateOne objects #js{:name (.-name new-shape)
                            :owner_id owner-id}
                    new-shape #js{:upsert true})
        (.then (fn [amount] (js/alert "success")))
        (.catch js/alert)
        )))

(defn on-js-reload [] )

(defn no-auth-buttons []
  [:span
   [:input.btn.btn-primary
    {:value "view" :type "button"
     :on-click (fn [] (if (nil? (.-v (@app-state :shape) ))
                        (js/alert "you must select a shape first")
                        (swap! app-state assoc :content
                               [model-viewer (@app-state :shape)]
                               )))}]
   [:input.btn.btn-primary
    {:value "browse" :type "button"
     :on-click #(swap! app-state assoc :content
                       (reagent/as-element [model-browser app-state]))
     }]]
  )
(defn auth-buttons [logged-in?]
  (if logged-in?
    [:span
     [:input.btn.btn-primary
      {:value "create" :type "button"
       :on-click #(swap! app-state assoc :content [creator send-shape])}]
     [:input.btn.btn-primary
      {:value "log out" :type "button"
       :on-click #(do (swap! app-state assoc :content [model-browser])
                      (swap! app-state assoc :logged-in false)
                      ;;we only pretend to log out, because stitch doesn't really let us
                      (swap! app-state (fn [state]
                                         (.logout (state :client))
                                         state)
                             ))}]]
    ;;else
    [:span
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
   [no-auth-buttons]
   [auth-buttons (@app-state :logged-in)]
   (@app-state :content)
   ])

(reagent/render-component
 [app]
 (. js/document (getElementById "app")))
