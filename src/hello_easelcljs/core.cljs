(ns hello-easelcljs.core)

(def quantity-property {:id "Circles" :type "range" :min 1 :max 100 :step 1 :value 50})

(def size-property {:id "Size" :type "range" :min 1 :max 100 :step 1 :value 50})

(defn- material-depth [js-args]
  (get-in (js->clj js-args) ["material" "dimensions" "z"]))

(defn- random-depth [material-thickness]
  (* material-thickness
     (/ (.ceil js/Math (* 8 (rand))) 16)))

(defn- canned-rectangle [material-thickness]
  {
   :shape {
           :type "rectangle"
           :flipping {}
           :center {:x 5 :y 5}
           :width 10
           :height 10
           :rotation 2
           }
   :cut {
         :depth (random-depth material-thickness)
         :type "outline"
         :outlineStyle "on-path"
         }
   })

(defn- generate-circles-within-canned-rectangle [material-thickness]
  (let [rectangle (canned-rectangle material-thickness)]
    [rectangle]))

(defn load-properties []
  (clj->js [quantity-property size-property]))

(defn executor [js-args js-success js-failure]
  (js-success (clj->js (generate-circles-within-canned-rectangle
                        (material-depth js-args)))))
