(ns calc-goggles.view
  (:require [reagent.core :as reagent]))
(defn model-viewer [shape]
  (reagent/create-class
   {:display-name "model-viewer"
    :reagent-render (fn [] [:div {:id "three"}])
    :component-did-mount (fn []
                           (print shape)
                           (js/viewModel shape))
    :component-will-unmount #(js/killViewer)}
   ))
