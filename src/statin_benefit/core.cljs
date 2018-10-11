(ns ^:figwheel-hooks statin-benefit.core
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [reagent.dom.server :as rds]
            [statin-benefit.config :as config]
            [statin-benefit.events :as events]
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
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))

(defn render-to-str []
  (rds/render-to-string [views/main-panel]))
