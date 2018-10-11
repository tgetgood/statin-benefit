(ns statin-benefit.subs
  (:require [re-frame.core :as re-frame]
            [statin-benefit.events :as events]
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

(def required-keys
  (->> (dissoc events/evs ::events/bp-diastolic)
       keys
       (map name)
       (map keyword)))

(defn valid? [x]
  (and (not (nil? x))
       (or (not (number? x)) (not (js/isNaN x)))))

(re-frame/reg-sub
 ::filled?
 (fn [db]
   (every? #(valid? (get db %)) required-keys)))

(re-frame/reg-sub
 ::valid?
 (fn [db [_ k]]
   (let [v (get db k)]
     (or (nil? v) (valid? v)))))
