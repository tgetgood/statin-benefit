(ns statin-benefit.loader
  "Messy logic of rendering to static html, and recovering the react state from
  the DOM in the event the internet is so slow that the user starts to fill the
  form before the js has loaded."
  (:require [goog.object :as obj]
            [react-dom :as react-dom]
            reagent.dom
            [reagent.dom.server :as rds]
            reagent.impl.batching
            reagent.impl.template
            reagent.impl.util
            [statin-benefit.validation :as validation]
            [statin-benefit.views :as views]
            [re-frame.core :as re-frame]))

(defn grab-int [id]
  (validation/int
   (obj/get (js/document.getElementById id) "value")))

(defn grab-radio [n]
  (let [sel (str "input[name='" (name n) "']:checked")]
    (when-let [elem (js/document.querySelector sel)]
      (when-let [val (obj/get elem "value")]
       val))))

(defn grab-bool [n]
  (when-let [val (grab-radio n)]
    (validation/bool val)))

(defn grab-select [id]
  (let [v (obj/get (js/document.getElementById id) "value")]
    (when-not (= "none" v)
      (keyword v))))

(def grabbers
  {validation/bool grab-bool
   validation/int  grab-int
   keyword         grab-select})

(defn grab-values-from-dom
  "Returns the values of all inputs in the dom. Only useful is the js is very
  slow to load."
  []
  (into (if-let [v (grab-radio :sex)]
          {:sex (keyword v)}
          {})
        (map (fn [[k t]]
               (let [v ((get grabbers t) (name k))]
                 (when (validation/valid? v)
                   [k v]))))
        (dissoc validation/fields :sex)))

(defn hydrate [component container]
  (let [comp  (fn [] (reagent.impl.template/as-element component))]
    (binding [reagent.impl.util/*always-update* true]
      (react-dom/hydrate
       (comp)
       container
       (fn []
         (re-frame/dispatch
          [:statin-benefit.events/initialize-db (grab-values-from-dom)])
         (binding [reagent.impl.util/*always-update* false]
           (swap! reagent.dom/roots assoc container [comp container])
           (reagent.impl.batching/flush-after-render)))))))

(defn dehydrate []
  (rds/render-to-string [views/main-panel]))
