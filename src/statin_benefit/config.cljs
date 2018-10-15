(ns statin-benefit.config
  (:require [goog.object :as obj]))

(def startup-lang
  (keyword (obj/get (js/document.querySelector "html") "lang")))
