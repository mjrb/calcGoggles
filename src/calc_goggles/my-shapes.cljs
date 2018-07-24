(ns calc-goggles.my-shapes
  (:require [reagent.core :as reagent :refer [atom]]
            [calc-goggles.stitch :as stitch]
            [calc-goggles.utils :as utils]))
(defonce my-shapes (atom []))
(defn get-user-shapes  [app-state]
  (-> (utils/get-objects-coll app-state)
      (.find #js{:owner_id (.authedId (:client app-state))}
             #js{:name true :_id true})
      (.execute)
      (.then (fn [shapes]
               (reset! my-shapes shapes)
               (print shapes)))
      (.catch js/alert)))

(defn delete-shape [id app-state-atom]
  (-> (utils/get-objects-coll @app-state-atom)
      (.deleteOne #js{:_id id})
      (.then (get-user-shapes app-state-atom))
      (.catch js/alert)))

(defn shape-list-item [shape app-state-atom]
  [:li 
   (utils/boot-btn-secondary "delete"
                             #(delete-shape (.-_id shape) app-state-atom))
   (.-name shape)])

(defn my-shapes-list [shapes app-state-atom]
  (into [:ul] (map
               (fn [shape] [shape-list-item shape app-state-atom])
               shapes)))

(defn render-browser [app-state-atom]
  [:div.container
   (if (empty? @my-shapes)
     [:div.alert-warning
      "No shapes found :( try making some by clicking create"]
     [my-shapes-list @my-shapes app-state-atom]
     )])

(defn my-shapes-browser [app-state-atom]
  (reagent/create-class
   {:display-name "my-shapes-browser"
    :reagent-render #(render-browser app-state-atom)
    :component-will-mount #(get-user-shapes @app-state-atom)}))
