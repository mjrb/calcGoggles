(ns calc-goggles.browse
  (:require [calc-goggles.stitch :as s]
            [calc-goggles.view :refer [model-viewer]]
            [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :as a]))
(defn feild-value [id] (.-value (.getElementById js/document id)))
(defonce objects (atom #js[]))
(defonce all-objects (atom #js[]))
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
        (.then (fn [objs]
                 (reset! objects objs)
                 (reset! all-objects objs)
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
  [:li 
    [:input.btn.btn-secondary {:type "button" :value "view" :style {:margin-bottom 2}
             :on-click #(view-object (.-_id object) app-state)}]
   (.-name object)
   ])
(defn object-list [app-state]
  (into [:ul]
        (map (fn [obj]
               (reagent/as-element [object-list-item obj app-state]))
             @objects)))
(defn contains [string other]
  (try
    (some? (re-find (re-pattern (str "(?i)" string)) other))
    (catch js/Error e e)
  ))
;TODO impl username based search
(defn obj-search []
  (let [query (feild-value "query")]
    (print "ss")
    (swap! objects #(filter (fn [obj] (contains query (.-name obj))) @all-objects))))

(defn model-browser [app-state]
  (possibly-update-objects app-state)
  (reagent/create-class
   {:display-name "model-browser"
    :reagent-render (fn [] [:div.container
                            [:input.form-control
                             {:id "query" :style {:width "70%" :display "inline-block"}
                              :placeholder "object name"
                              :on-change obj-search}]
                            [:input.btn.btn-primary {:type "button" :value "search!"
                                                     :on-click obj-search}]
                            (if (not-empty @objects)
                              (reagent/as-element [object-list app-state])
                              [:span [:br][:div.alert-warning "No objects found :( sorry"]])])
    :component-will-unmount #(reset! objects #js[]) }
   ))
