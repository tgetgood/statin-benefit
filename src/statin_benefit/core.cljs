(ns ^:figwheel-hooks statin-benefit.core
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [statin-benefit.config :as config]
            [statin-benefit.events :as events]
            [statin-benefit.loader :as loader]
            [statin-benefit.views :as views]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn ^:after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [::events/initialize-db {:c-units :mmol-l}])
  (dev-setup)
  (mount-root))

(defn ^:export hydrate []
  (loader/hydrate [views/main-panel] (.getElementById js/document "app")))
