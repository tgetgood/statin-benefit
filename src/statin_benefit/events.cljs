(ns statin-benefit.events
  (:require
   [re-frame.core :as re-frame]
   [statin-benefit.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
