(defproject hello-easelcljs "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]]
  :plugins [[lein-figwheel "0.5.0-1"]
            [lein-cljsbuild "1.1.3"]]
  :clean-targets [:target-path "out"]
  :profiles {:dev {
                   :cljsbuild {
                               :builds [{:id "dev"
                                         :source-paths ["src"]
                                         :figwheel true
                                         :compiler {:main "hello-easelcljs.core"}}]
                               }}
             :optimized {
                         :cljsbuild {
                                     :builds [{:id "dev"
                                               :source-paths ["src"]
                                               :compiler {:main "hello-easelcljs.core"
                                                          :optimizations :whitespace}}]
                                     }}})
