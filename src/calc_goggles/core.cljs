(ns calc-goggles.core
  (:require [reagent.core :as reagent :refer [atom]]
            [calc-goggles.create :refer [creator plot]]
            [calc-goggles.view :refer [model-viewer]]
            [calc-goggles.browse :refer [model-browser]]))
(enable-console-print!)
(defn lmao [] [:div "sss"])

(defn widget1 []
  [:div.alert-primary "this is widget 1"])
(defn widget2 []
  [:div.alert-danger "this is widget 2"])

(defonce app-state (atom {:text "Hello world!"}))
(defonce shape (atom #js{}))
(defn set-shape [new-shape]
  (print @shape)
  (reset! shape new-shape)
  (print @shape))
(defonce content (atom [creator set-shape]))
(defn thr []
  widget1)
(defn on-js-reload [] )


(defn content-comp [cont]
  (case cont
    1 [widget1]
    2 [widget2]
    :br))
(defn app []
  [:div
  [:input.btn.btn-primary
   {:value "create" :type "button"
    :on-click #(reset! content [creator set-shape])}]
  [:input.btn.btn-primary
   {:value "view" :type "button"
    :on-click #(reset! content [model-viewer @shape])}]
   [:input.btn.btn-primary
   {:value "browse" :type "button"
    :on-click #(reset! content [model-browser])}]
   @content
   ])

(reagent/render-component
 [app]
 (. js/document (getElementById "app")))
