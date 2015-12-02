(defproject pixelator "0.1.0-SNAPSHOT"
  :description "Pixelator!"
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-middleware-format "0.7.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [digest "1.4.4"]
                 [compojure "1.4.0"]
                 [figwheel-sidecar "0.5.0-SNAPSHOT" :scope "test"]
                 [org.clojure/data.json "0.2.6"]
                 [net.mikera/imagez "0.9.0"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-2"]]
  :ring {:handler pixelator.core/app
         :nrepl {:start? true
                 :port 9998}}
  :clean-targets ^{:protect false} ["resources/public/js" 
                                    :target-path]
  :resource-paths ["resources"]
  :main pixelator.core
  :uberjar-name "pixelator.jar"
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :figwheel true
                        :compiler {:main "pixelator.core"
                                   :asset-path "js/out"
                                   :output-to "resources/public/js/main.js"
                                   :output-dir "resources/public/js/out"}}
                       {:id "min"
                        :source-paths ["src/cljs"]
                        :jar true
                        :compiler {:main "pixelator.core"
                                   :asset-path "js/out"
                                   :optimizations :advanced
                                   :output-to "resources/public/js/main.js"
                                   :output-dir "resources/public/js/release"}}]}
  :figwheel {:http-server-root "public"
             :css-dirs ["resources/public/css"]
             :ring-handler pixelator.core/app}
  :auto-clean false
  :min-lein-version "2.0.0"
  :aliases {"stand-alone" ["do" "clean" ["cljsbuild" "once" "min"] "uberjar"]})
