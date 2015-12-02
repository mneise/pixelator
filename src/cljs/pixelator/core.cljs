(ns pixelator.core
  (:require [goog.dom :as gdom]
            [goog.events :as events])
  (:import goog.net.IframeIo
           goog.net.EventType))

(enable-console-print!)

(defn on-success [response]
  (let [app (gdom/getElement "app")
        new-img (gdom/createDom "img" #js {:src (aget response "tempfile")
                                           :id "pixel-image"})]
    (when-let [old-img (gdom/getElement "pixel-image")]
      (gdom/removeNode old-img))
    (gdom/append app new-img)))

(defn upload-file []
  (let [el (gdom/getElement "upload-form")
        iframe (IframeIo.)]
    (events/listen iframe EventType.COMPLETE
                   (fn [event]
                     (let [response (.getResponseJson iframe)]
                       (when (= (aget response "status") "OK")
                         (on-success response)))
                     (.dispose iframe)))
    (.sendFromForm iframe el "/upload")))


(defn create []
  (let [header (gdom/createDom "div" #js {:id "header"})
        form (gdom/createDom "form" #js {:id "upload-form"
                                         :enctype "multipart/form-data"
                                         :method "POST"})
        input (gdom/createDom "input" #js {:type "file"
                                           :name "upload-file"
                                           :id "upload-file"})
        label (gdom/createDom "label" #js {:for "block-size"}
                              "Block Size (in pixel)")
        block-size (gdom/createDom "input" #js {:type "number"
                                                :name "block-size"
                                                :id "block-size"})
        button (gdom/createDom "button" #js {:onclick #(upload-file)
                                             :id "submit"}
                               "Create Pixel Art")]
    (gdom/append (gdom/getElement "app") header)
    (gdom/append header form)
    (gdom/append header button)
    (gdom/append form input)
    (gdom/append form label)
    (gdom/append form block-size)))

(create)
