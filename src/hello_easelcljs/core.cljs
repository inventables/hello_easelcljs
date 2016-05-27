(ns hello-easelcljs.core
  (:require [clojure.walk]))

(def quantity-property {:id "Circles" :type "range" :min 1 :max 100 :step 1 :value 50})

(def size-property {:id "Size" :type "range" :min 1 :max 100 :step 1 :value 50})

(defn- volume-js->cljs [js-volume]
  (-> js-volume
      js->clj
      clojure.walk/keywordize-keys))

(defn volume-intersection [vol-a vol-b]
  (clojure.walk/keywordize-keys
   (js->clj
    ((aget js/EASEL "volumeHelper" "intersect")
     (clj->js [vol-a])
     (clj->js [vol-b])))))

(defn- bounding-box [volume]
  (clojure.walk/keywordize-keys
   (js->clj ((aget js/EASEL "volumeHelper" "boundingBox")  (clj->js [volume])))))

(defn- material-depth [js-args]
  (aget js-args "material" "dimensions" "z"))

(defn- quantity-arg [js-args]
  (aget js-args "params" "Circles"))

(defn- size-arg [js-args]
  (aget js-args "params" "Size"))

(defn- selected-volume-ids-arg [js-args]
  (js->clj (aget js-args "selectedVolumeIds")))

(defn- volumes-arg [js-args]
  (clojure.walk/keywordize-keys
   (js->clj (aget js-args "volumes"))))

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
  (->> circle-volumes
       (map (fn[v]
              (let [clipped (volume-intersection design-volume v)]
                (if (nil? clipped)
                  nil
                  (assoc clipped :cut (:cut v))))))
       (filter (complement nil?))))

(defn- generate-circles-within-canned-rectangle [material-thickness
                                                 quantity
                                                 size-param]
  (let [rectangle (canned-rectangle material-thickness)
        circles (build-circles rectangle material-thickness quantity size-param)]
    (into [rectangle] (intersect rectangle circles))))

(defn- selected-volumes [input-volumes selected-volume-ids]
  (let [vids (set selected-volume-ids)]
    (filter (fn[v] (vids (:id v))) input-volumes)))

(defn- generate-circles-within-selected-shapes [material-thickness
                                                quantity
                                                size-param
                                                input-volumes
                                                selected-volume-ids]
  (mapcat (fn[vol]
            (let [circles (build-circles vol
                                         material-thickness
                                         quantity
                                         size-param)
                  updated-volume (assoc vol :cut (merge (:cut vol)
                                                  {:type "outline"
                                                   :outlineStyle "outside"}))]

             (into [updated-volume] (intersect vol circles))))
          (selected-volumes input-volumes selected-volume-ids)))

(defn load-properties []
  (clj->js [quantity-property size-property]))

(defn executor [js-args js-success js-failure]
  (let [selected-volume-ids (selected-volume-ids-arg js-args)
        depth (material-depth js-args)
        quantity (quantity-arg js-args)
        size (size-arg js-args)
        ret (clj->js (if (< 0 (count selected-volume-ids))
                       (generate-circles-within-selected-shapes
                        depth
                        quantity
                        size
                        (volumes-arg js-args)
                        selected-volume-ids)
                       (generate-circles-within-canned-rectangle
                        depth
                        quantity
                        size)))]
    (js-success ret)))
