(ns calc-goggles.browse
  (:require [calc-goggles.stitch :as s]
            [calc-goggles.view :refer [model-viewer]]
            [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :as a]
            [calc-goggles.utils :as utils :refer [feild-value contains]]))
(defonce objects (atom #js[]))
(defonce all-objects (atom #js[]))

(defn fetch-objects [app-state-atom]
  (-> (utils/get-objects-coll @app-state-atom)
      (.find #js{}
             #js{:name true :_id true})
      (.limit 500)
      (.execute)
      (.then (fn [objs]
               (reset! objects objs)
               (reset! all-objects objs)
               ))
      (.catch #(js/alert (str "model-browser querry error: " %)))
      ))

(defn view-object [id app-state-atom]
  (-> (utils/get-objects-coll @app-state-atom)
      (.findOne #js{:_id id})
      (.then #(utils/set-content! app-state-atom [model-viewer %]))
      (.catch #(js/alert %))
      ))

;;TODO impl username based search
(defn name-filter [obj]
  (contains query (.-name obj)))
(defn local-search []
  (let [query (feild-value "query")]
    (swap! objects #(filter name-filter  @all-objects))))
;;TODO have this actualy goto db to get more results
(defn db-search []
  (let [query (feild-value "query")]
    (swap! objects #(filter name-filter @all-objects))))

;;components
(defn object-list-item [object app-state-atom]
  [:li 
    (utils/boot-btn-secondary "view" 
                              #(view-object (.-_id object) app-state-atom)
                              {:style {:margin-bottom 2}})
   (.-name object)])

(defn object-list [app-state-atom]
  (into [:ul]
        (map (fn [obj]
               [object-list-item obj app-state-atom])
                   @objects)))

(defn render-browser [app-state-atom]
  [:div.container
   [:input.form-control
    {:id "query" :style {:width "70%" :display "inline-block"}
     :placeholder "object name"
     :on-change local-search}]
   (utils/boot-btn-primary "search!" db-search)
   (if (not-empty @objects)
     (reagent/as-element [object-list app-state-atom])
     [:span [:br][:div.alert-warning "No objects found :( sorry"]])])

(defn model-browser [app-state-atom]
  (reagent/create-class
   {:display-name "model-browser"
    :reagent-render #(render-browser app-state-atom)
    :component-will-mount #(fetch-objects app-state-atom)}))
