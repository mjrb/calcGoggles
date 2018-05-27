(ns calc-goggles.create
  (:require [reagent.core :as reagent :refer [atom]]
            [clojure.string :refer [blank?]]
            [calc-goggles.view :refer [model-viewer]]
            [calc-goggles.utils :refer [feild-value label
                                        str-is-float? str-is-int?
                                        draw make-shape]]))

;;;;;;;;;;;;;;;;;;; state ;;;;;;;;;;;;;;;;;;;
;; these strings are classes to add to form inputs. if its is-invalid the form input is red
(defonce valid (atom {:fn "" :minx "" :maxx "" :number "" :vscale "" :name "is-invalid"}))

;;the list of current errors
(defonce errors (atom {:name "name cant be null"}))

;;state of radio
(defonce checked (atom {:square true :rtrig false}))

(defn reset-state []
  (reset! valid {:fn "" :minx "" :maxx "" :number "" :vscale "" :name "is-invalid"})
  (reset! errors {:name "name cant be null"})
  (reset! checked {:square true :rtrig false})
  )

;;;;;;;;;;;;;;;; validators ;;;;;;;;;;;;;;;;

(defn validate-fn []
  (try
    ;;try to eval it with x at 1 to see if we get an error
    (let [func (feild-value "eq")]
      (if (blank? func) (throw "Must have a function"))
      (js/limitedEval func #js{:x 1}))
    (draw)
    (swap! valid #(assoc %1 :fn ""))
    (swap! errors #(dissoc %1 :fn))
    (catch :default e
      (print "bad fn" e)
      (swap! valid #(assoc %1 :fn "is-invalid"))
      (swap! errors #(assoc %1 :fn (str "function: " e)))
      )))

(defn validate-number [id, key]
  (fn []
    (let [value (feild-value id)]
      (if (and (not (blank? value))
               (str-is-float? value) )
        (do (swap! valid #(assoc %1 key ""))
            (swap! errors #(dissoc %1 key))
            (draw))
        (do (swap! valid #(assoc %1 key "is-invalid"))
            (swap! errors #(assoc %1 key (str id " must be number"))))
        ))))

(defn validate-name []
  (let [value (feild-value "name")]
    (if (not (blank? value))
      (do (swap! valid #(assoc %1 :name ""))
          (swap! errors #(dissoc %1 :name)))
      (do (swap! valid #(assoc %1 :name "is-invalid"))
          (swap! errors #(assoc %1 :name "name can't be empty")))
      )))

(defn validate-xsections[]
  (let [value (feild-value "number")]
    (if (and (not (blank? value))
             (str-is-int? value)
             (>= (js/parseInt value) 5))
      (do (swap! valid #(assoc %1 :number ""))
          (swap! errors #(dissoc %1 :number )))
      (do (swap! valid #(assoc %1 :number "is-invalid"))
          (swap! errors #(assoc %1 :number "cross Sections must be whole number greater than 5")))
      )))
(defn validate-shape [shape-handler]
  (fn []
    (if (empty? @errors)
      (shape-handler (make-shape))
      (js/alert "please fix the red feilds first")
      )
    ))

;;;;;;;;;;;;;;;;;;; components ;;;;;;;;;;;;;;;;;;;
(defn func-inputs []
  [:div
   (label "Enter a Function: " "eq"
          [:input.form-control {:type "text" :id "eq"
                                :default-value "4 * sin(x) + 5 * cos(x/2)"
                                :on-change validate-fn :class-name (:fn @valid)}])
   (label "Min x: " "minx"
          [:input.form-control {:type "number" :id "minx" :default-value "-5"
                                :on-change (validate-number "minx" :minx) :step "any"
                                :class-name (:minx @valid)}])
   (label "Max x: " "maxx"
          [:input.form-control {:type "number" :id "maxx" :default-value "5"
                                :on-change (validate-number "maxx" :maxx) :step "any"
                                :class-name (:maxx @valid)}])
   (label "Vertical Scale: " "vscale"
          [:input.form-control {:type "number" :id "vscale" :default-value "1"
                                :on-change (validate-number "vscale" :vscale) :step "any"
                                :class-name (:vscale @valid)}])
   ])
(defn check [key]
  (if (= key :square)
    (fn [] (swap! checked #(assoc %1 :square true :rtrig false)))
    (fn [] (swap! checked #(assoc %1 :square false :rtrig true)))
    ))
(defn xsection-radio []
  [:span
   [:label {:for "rtrig"} "Right Acute Triangle"]
   [:input {:type "radio" :id "rtrig" :name "xsection" :value "rtrig"
            :checked (:rtrig @checked)
            :on-change (check :rtrig)}]
   
   [:label {:for "square"} "Square"]
   [:input {:type "radio" :id "square" :name "xsection" :value "square"
            :checked (:square @checked)
            :on-change (check :square)}]
   ])

(defn export-inputs []
  [:div
   (label "Shape Name: " "name"
          [:input.form-control {:type "text" :id "name"
                                :on-change validate-name :class-name (:name @valid)}])
   (label "Number of Cross Sections" "number"
          [:input.form-control {:type "number" :id "number" :default-value "6"
                   :on-change validate-xsections
                   :class-name (:number @valid)}])
   (label "Cross Section Type: ")
   [xsection-radio]
   ])

(defn creator-form [shape-handler]
  [:form.col-md-6
   [func-inputs]
   [export-inputs]
   [:br]
   [:input.btn.btn-primary {:type "button" :on-click (validate-shape shape-handler) :value "Make Shape!"}]
   ])

(defn error-list []
  (if (not-empty @errors)
    [:ul
     (map (fn [err] [:li.alert-danger err]) @errors)]
    [:br]))

(defn plot []
  (reagent/create-class
   {:component-did-mount draw
    :reagent-render (fn [] [:div.col-md-6 {:id "plot"}])
    :display-name "plot"
    }))

;; this is the top level component
(defn creator [shape-handler]
  (reagent/create-class
   {:reagent-render (fn [] [:div
                       [:div.row
                        [creator-form shape-handler]
                        [plot]]
                       [error-list]
                            ])
    :component-will-unmount reset-state
    }))
