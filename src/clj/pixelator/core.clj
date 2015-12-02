(ns pixelator.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware
             [defaults :refer [site-defaults wrap-defaults]]
             [format :refer [wrap-restful-format]]]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [mikera.image
             [core :as core]
             [filters :as filters]
             [colours :as colours]]
            [digest :as digest])
  (:gen-class))

(def default-block-size 20)

(defn make-response [status value-map]
  {:status status
   :headers {"Content-Type" "application/json"}
   :body (json/write-str value-map)})

(defn cart [colls]
  (if (empty? colls)
    '(())
    (for [x (first colls)
          more (cart (rest colls))]
      (cons x more))))

(defn handle-upload [{:keys [filename tempfile] :as upload-file} block-size]
  (if (and tempfile filename (not (str/blank? filename)))
    (let [resource-path (str "images/" (digest/sha-256 (str tempfile block-size)) ".png")
          dest-path (str "resources/public/" resource-path)
          img (core/load-image tempfile)
          width (core/width img)
          height (core/height img)
          block-size (or (when (and block-size (not (str/blank? block-size)))
                           (read-string block-size))
                         default-block-size)]
      (when (> block-size 0)
        (loop [x 0
               y 0]
          (let [x-end (if (> (+ x block-size) width) width (+ x block-size))
                y-end (if (> (+ y block-size) height) height (+ y block-size))
                idxs (cart [(range x x-end) (range y y-end)])
                pixels (->> idxs
                            (map #(core/get-pixel img (first %) (second %)))
                            (map colours/components-rgb))
                rgbs (partition (count pixels) (apply interleave pixels))
                average (map #(/ (apply + %) (count pixels)) rgbs)]
            (doseq [idx idxs]
              (core/set-pixel img (first idx) (second idx) (colours/rgb-from-components (nth average 0)
                                                                                        (nth average 1)
                                                                                        (nth average 2))))
            (if (and (< x-end width) (< y-end height))
              (recur x-end y)
              (if (and (= x-end width) (< y-end height))
                (recur 0 (+ y-end))
                (if (and (< x-end width) (= y-end height))
                  (recur x-end y)
                  img))))))
      (core/save img dest-path)
      (make-response 200 {:status "OK"
                          :tempfile resource-path}))
    (make-response 400 {:status "No file"})))

(defroutes routes
  (GET "/" [] (slurp (io/resource "public/index.html")))
  (POST "/upload" [upload-file block-size] (handle-upload upload-file block-size))
  (resources "/")
  (not-found (make-response 404 {:status "Not Found"})))

(def app
  (-> routes
      wrap-restful-format
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))

(defn -main []
  (run-jetty app {:port 3000}))
