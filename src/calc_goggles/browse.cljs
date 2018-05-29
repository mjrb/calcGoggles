(ns calc-goggles.browse
  (:require [calc-goggles.stitch :as s]
            [reagent.core :as reagent]
            [cljs.core.async :as a]))
(defonce objects (atom #js[#js{:name "one"} #js{:name "two"} #js{:name "three"}] ))
(defonce re-poll (atom true))
(defn reset []
  (reset! objects #js[])
  (re-poll))

(defn update-objects [app-state]
  (let [objects-coll
          (s/atlas-db-coll (:client @app-state) ( :db-name @app-state) "objects")]
    (-> (.find objects-coll #js{} #js{:name true})
        (.execute)
        (.then (fn [objs] (reset! objects objs)
                 ;;i probably shouldn't do this, but ... idk
                 (reagent/force-update-all)
                 (print @objects)
                 ))
        (.catch #(js/alert (str "model-browser querry error: " %)))
        )))
(defn possibly-update-objects [app-state]
    (if @re-poll
        (do (update-objects app-state)
            (reset! re-poll false)
            (print "here1")
            )
        (do (reset! re-poll true)
            (print "here2")
           )))
(defn model-browser [app-state]
  (print "h1")
  (possibly-update-objects app-state)
  (print "h2")
 ; (reagent/create-class
 ;  {:display-name "model-browser"
 ;   :reagent-render (fn [] (print "h3")
                      (into [:ul] (map (fn [obj]
                                         [:li (.-name obj)])
                                       @objects)
                            ))
 ;   :component-will-unmount #(print "unmount") }
;   ))
(a/go-loop []
  (a/<! (a/timeout 1000))
  (print @objects))
