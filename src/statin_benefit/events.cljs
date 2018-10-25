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

(re-frame/reg-event-db
 ::ezetimibe?
 (fn [db [_ e]]
   (assoc db :ezetimibe? e)))

(re-frame/reg-event-db
 ::currently-on-statins?
 (fn [db]
   ))

(re-frame/reg-event-db
 ::change-language
 (fn [db [_ lang]]
   (assoc db :lang lang)))
