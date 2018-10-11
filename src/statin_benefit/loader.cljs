(ns statin-benefit.loader
  (:require [react-dom :as react-dom]
            reagent.dom
            [reagent.dom.server :as rds]
            reagent.impl.batching
            reagent.impl.template
            reagent.impl.util
            [statin-benefit.views :as views]))

(defn grab-values-from-dom
  "Returns the values of all inputs in the dom. Only useful is the js is very
  slow to load."
  []
  (into
   {}
   (map (fn [[k v]]
          [(keyword (name k))
           #_(grab-val)]))))

(defn hydrate [component container]
  (let [comp  (fn [] (reagent.impl.template/as-element component))]
    (binding [reagent.impl.util/*always-update* true]
      (react-dom/hydrate
       (comp)
       container
       (fn []

         (binding [reagent.impl.util/*always-update* false]
           (swap! reagent.dom/roots assoc container [comp container])
           (reagent.impl.batching/flush-after-render)))))))

(defn dehydrate []
  (rds/render-to-string [views/main-panel]))
