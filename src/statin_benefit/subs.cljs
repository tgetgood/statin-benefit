(ns statin-benefit.subs
  (:require
   [re-frame.core :as re-frame]
   [statin-benefit.risk :as risk]))

(re-frame/reg-sub
 ::untreated-survival
 (fn [db]
   (risk/untreated-survival db)))

(re-frame/reg-sub
 ::untreated-risk
 :<- [::untreated-survival]
 (fn [v _]
   (- 1 v)))

(re-frame/reg-sub
 ::treated-survival
 (fn [db]
   (risk/treated-survival db)))

(re-frame/reg-sub
 ::treated-risk
 :<- [::treated-survival]
 (fn [v _]
   (- 1 v)))

(re-frame/reg-sub
 ::reduction
 :<- [::treated-risk]
 :<- [::untreated-risk]
 (fn [[treated untreated] _]
   (- untreated treated)))

(re-frame/reg-sub
 ::reduction-percentage
 :<- [::untreated-risk]
 :<- [::reduction]
 (fn [[untreated reduction] _]
   (/ reduction untreated)))
