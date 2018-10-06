(ns statin-benefit.events
  (:refer-clojure :exclude [int])
  (:require [re-frame.core :as re-frame]
            [statin-benefit.db :as db]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   {}))

(defn int [x]
  (js/parseInt x))

(defn bool [x]
  (= "1" x))

(def evs
  {::age          int
   ::sex          keyword
   ::ethnicity    keyword
   ::bp-systolic  int
   ::bp-diastolic int
   ::hypertension bool
   ::total-c      int
   ::ldl-c        int
   ::hdl-c        int
   ::c-units      keyword
   ::smoke?       bool
   ::diabetic?    bool})

(run! (fn [[ev format]]
        (re-frame/reg-event-db
         ev
         (fn [db [_ n]]
           (assoc db (keyword (name ev)) (format n)))))
      evs)
