(ns ^:figwheel-hooks statin-benefit.core
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [statin-benefit.events :as events]
            [statin-benefit.loader :as loader]
            [statin-benefit.views :as views]))

(defn ^:after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(def dev-default-db
  {:c-units :mmol-l
   :lang    :en})

(defn init []
  (enable-console-print!)
  (re-frame/dispatch-sync [::events/initialize-db dev-default-db])
  (mount-root))

(defn ^:export hydrate []
  (loader/hydrate [views/main-panel] (.getElementById js/document "app")))
