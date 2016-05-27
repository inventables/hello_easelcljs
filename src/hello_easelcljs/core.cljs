(ns hello-easelcljs.core
  (:require [clojure.walk]))

(def quantity-property {:id "Circles" :type "range" :min 1 :max 100 :step 1 :value 50})

(def size-property {:id "Size" :type "range" :min 1 :max 100 :step 1 :value 50})

(defn- volume-js->cljs [js-volume]
  (-> js-volume
      js->clj
      clojure.walk/keywordize-keys))

(defn- bounding-box [volume]
  (clojure.walk/keywordize-keys
   (js->clj ((aget js/EASEL "volumeHelper" "boundingBox")  (clj->js [volume])))))

(defn- material-depth [js-args]
  (aget js-args "material" "dimensions" "z"))

(defn- quantity-arg [js-args]
  (aget js-args "params" "Circles"))

(defn- size-arg [js-args]
  (aget js-args "params" "Size"))

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
         }})

(defn random-position-inside [constraining-volume]
  (let [bb (bounding-box constraining-volume)]
    {:x (+ (:left bb) (* (rand) (:width bb)))
     :y (+ (:bottom bb) (* (rand) (:height bb)))}))

(defn random-size-within [constraining-volume size-param]
  (let [bb (bounding-box constraining-volume)
        max-size (min (:width bb) (:height bb))]
    (.log js/console "Bounding Box is " (clj->js bb))
    (* (rand) max-size (/ size-param 100))))

(defn- build-circles [constraining-volume
                      material-thickness
                      quantity
                      size-param]
  (map
   (fn[i]
     (let [size (random-size-within constraining-volume size-param)]
       {:shape {
                :type "ellipse"
                :center (random-position-inside constraining-volume)
                :width size
                :height size
                :flipping {}
                :rotation 0
                },
        :cut {
              :depth (random-depth material-thickness)
              :type (if (> 0.5 (rand)) "outline" "fill")
              :outlineStyle "on-path"
              }}))
   (range quantity)))

(defn intersect [design-volume circle-volumes]
  circle-volumes)

(defn- generate-circles-within-canned-rectangle [material-thickness
                                                 quantity
                                                 size-param]
  (let [rectangle (canned-rectangle material-thickness)
        circles (build-circles rectangle material-thickness quantity size-param)]
    (into [rectangle] (intersect rectangle circles))))

(defn load-properties []
  (clj->js [quantity-property size-property]))

(defn executor [js-args js-success js-failure]
  (let [ret (clj->js (generate-circles-within-canned-rectangle
                      (material-depth js-args)
                      (quantity-arg js-args)
                      (size-arg js-args)))]
    (.log js/console "Size is " (aget js-args "params" "Size"))
    (.log js/console ret)
    (js-success ret)))
