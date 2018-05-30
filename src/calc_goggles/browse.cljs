(ns calc-goggles.browse
  (:require [calc-goggles.stitch :as s]
            [calc-goggles.view :refer [model-viewer]]
            [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :as a]))
(defonce objects (atom #js[] ))
(print (empty? @objects))
(defonce re-poll (atom true))
(defn reset []
  (reset! objects #js[])
  (re-poll))

(defn update-objects [app-state]
  (let [objects-coll
          (s/atlas-db-coll (:client @app-state) ( :db-name @app-state) "objects")]
    (-> (.find objects-coll #js{} #js{:name true :_id true})
        (.execute)
        (.then (fn [objs] (reset! objects objs)
                 ;;i probably shouldn't do this, but ... idk
                 (print @objects)
                 ))
        (.catch #(js/alert (str "model-browser querry error: " %)))
        )))
(defn possibly-update-objects [app-state]
    (if (empty? @objects)
      (do (update-objects app-state)
          )))

(defn view-object [id app-state]
  (-> (s/atlas-db-coll (@app-state :client) (@app-state :db-name) "objects")
      (.findOne #js{:_id id})
;      (.execute)
      (.then #(swap! app-state assoc :content (reagent/as-element [model-viewer %])))
      (.catch #(js/alert %))
      ))

(defn object-list-item [object app-state]
  [:li (.-name object)
    [:input {:type "button" :value "view"
            :on-click #(view-object (.-_id object) app-state)}]
   ])

(defn model-browser [app-state]
  (possibly-update-objects app-state)
  (reagent/create-class
   {:display-name "model-browser"
    :reagent-render (fn [] (if (not-empty @objects)
                      (into [:ul] (map (fn [obj]
                                         (reagent/as-element [object-list-item obj app-state]))
                                         @objects))
                      [:ul [:li "loading"] [:li "objects"]]))
    :component-will-unmount #(reset! objects #js[]) }
   ))
