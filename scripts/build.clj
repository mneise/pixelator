(require '[cljs.build.api :as b])

(println "Building ...")

(let [start (System/nanoTime)]
  (b/build "src"
    {:main 'pixelator.core
     :output-to "resources/public/js/main.js"
     :output-dir "resources/public/js"
     :asset-path "js"
     :verbose true
     :pretty-print true})
  (println "... done. Elapsed" (/ (- (System/nanoTime) start) 1e9) "seconds"))
