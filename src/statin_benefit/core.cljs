(ns ^:figwheel-hooks statin-benefit.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [statin-benefit.events :as events]
   [statin-benefit.views :as views]
   [statin-benefit.config :as config]))


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
