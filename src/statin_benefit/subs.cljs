(ns statin-benefit.subs
  (:require [re-frame.core :as re-frame]
            [statin-benefit.risk :as risk]
            [statin-benefit.validation :as validation]))

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
 ::risk-reduction
 :<- [::treated-risk]
 :<- [::untreated-risk]
 (fn [[treated untreated] _]
   (- untreated treated)))

(re-frame/reg-sub
 ::risk-reduction-percentage
 :<- [::untreated-risk]
 :<- [::risk-reduction]
 (fn [[untreated reduction] _]
   (/ reduction untreated)))

(re-frame/reg-sub
 ::filled?
 (fn [db]
   (every? #(validation/valid? (get db %)) validation/required-keys)))

(re-frame/reg-sub
 ::validation
 (fn [db [_ k]]
   (let [v (get db k)]
     (if (nil? v)
       "incomplete"
       (when-not (validation/valid? v)
         "invalid")))))
