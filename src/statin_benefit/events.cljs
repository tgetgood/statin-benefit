(ns statin-benefit.events
  (:refer-clojure :exclude [int])
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ [_ v]]
   v))

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
   ::smoker?      bool
   ::diabetic?    bool
   ::intensity    keyword})

(run! (fn [[ev format]]
        (re-frame/reg-event-db
         ev
         (fn [db [_ n]]
           (assoc db (keyword (name ev)) (format n)))))
      evs)
