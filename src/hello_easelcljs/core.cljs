(ns hello-easelcljs.core)

(defn load-properties []
  (clj->js [{:type "range", :id "Width", :value 4, :min 1, :max 10, :step 1}
            {:type "range", :id "Height", :value 4, :min 1, :max 10, :step 1}
            {:type "range", :id "Radius", :value 0, :min 0, :max 10, :step 1}
            {:type "range", :id "Something else", :value 0, :min 0, :max 10, :step 1}]))
