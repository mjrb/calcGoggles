(ns calc-goggles.utils
  (:require [calc-goggles.stitch :as s]
            [reagent.core :as reagent]))
;;;;;;;;;;;;;; convinience functions ;;;;;;;;;;;;;;;

(defn feild-value [id] (.-value (.getElementById js/document id)))
(defn str-is-float? [string]
  (-> string
      (js/parseFloat)
      (js/isNaN)
      (not)))
(defn draw [] (.  js/modelMaker draw))
(defn make-shape [] (. js/modelMaker makeShape))
(defn str-is-int? [string]
  (some? (re-matches #"^[0-9]+$" string) ))
;;wraps input with a label and br
(defn label
  ([label-text id elem-vec]
  [:span
   [:label {:for id} label-text]
   elem-vec
   [:br]])
  ([label-text]
   [:span
    [:label label-text]
    [:br]]))

(defn contains [string other]
  (try
    (some? (re-find (re-pattern (str "(?i)" string)) other))
    (catch js/Error e e)
  ))


(defn get-objects-coll [app-state]
  "takes in app-state hashmap and gives a stitch/mongo collection"
  (s/atlas-db-coll (:client app-state)
                   (:db-name app-state)
                   "objects"))
(defn boot-btn
  "creates bootstrap button component"
  ([value on-click]
   (boot-btn value on-click {}))
  ([value on-click props]
   [:input.btn (conj {:type "button"
                      :value value
                      :on-click on-click}
                     props)]))
(defn boot-btn-class
  "makes a bootstrap button with a given class"
  ([class value on-click]
   (boot-btn-class class value on-click {}))
  ([class value on-click props]
   (let [class-name (str class " " (:class-name props))]
     (boot-btn value on-click (assoc props :class-name class-name)))))
(def boot-btn-primary (partial boot-btn-class "btn-primary"))
(def boot-btn-secondary (partial boot-btn-class "btn-secondary"))

(defn set-content! [app-state-atom hiccup]
  (swap! app-state-atom assoc :content (reagent/as-element hiccup)))
