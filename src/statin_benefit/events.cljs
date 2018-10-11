(ns statin-benefit.events
  (:require [re-frame.core :as re-frame]
            [statin-benefit.validation :as validation]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ [_ v]]
   v))

(run! (fn [[ev format]]
        (re-frame/reg-event-db
         (keyword (namespace ::x) ev)
         (fn [db [_ n]]
           (assoc db ev (format n)))))
      validation/fields)
