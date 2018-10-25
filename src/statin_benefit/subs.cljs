(ns statin-benefit.subs
  (:require [re-frame.core :as re-frame]
            [statin-benefit.risk.ten :as risk]
            [statin-benefit.risk.thirty :as risk30]
            [statin-benefit.validation :as validation]))

;;;;; Form

(re-frame/reg-sub
 ::lang
 (fn [db]
   (:lang db)))

(re-frame/reg-sub
 ::filled?
 (fn [db]
   (every? #(validation/valid? (get db %)) validation/required-keys)))

(re-frame/reg-sub
 ::validation
 (fn [db [_ k]]
   (let [v (get db k)]
     (if (nil? v)
       " incomplete"
       (when-not (validation/valid? v)
         " invalid")))))

;;;;; 10 year risk

(re-frame/reg-sub
 ::untreated-ten-year-risk
 (fn [db]
   (- 1 (risk/untreated-survival db))))

(re-frame/reg-sub
 ::treated-ten-year-risk
 (fn [db]
   (- 1 (risk/treated-survival db))))

(re-frame/reg-sub
 ::number-to-treat-ten-years
 :<- [::treated-ten-year-risk]
 :<- [::untreated-ten-year-risk]
 (fn [[treated untreated] _]
   (/ 1 (- untreated treated))))

(re-frame/reg-sub
 ::ten-year-risk-reduction-percentage
 :<- [::untreated-ten-year-risk]
 :<- [::treated-ten-year-risk]
 (fn [[untreated treated] _]
   (/ (- untreated treated) untreated)))

;;;;; 30 year risk

(re-frame/reg-sub
 ::untreated-thirty-year-risk
 (fn [db]
   (risk30/untreated-risk db)))

(re-frame/reg-sub
 ::treated-thirty-year-risk
 (fn [db]
   0))

(re-frame/reg-sub
 ::number-to-treat-thirty-years
 :<- [::treated-thirty-year-risk]
 :<- [::untreated-thirty-year-risk]
 (fn [[treated untreated] _]
   (/ 1 (- untreated treated))))

(re-frame/reg-sub
 ::thirty-year-risk-reduction-percentage
 :<- [::untreated-thirty-year-risk]
 :<- [::treated-thirty-year-risk]
 (fn [[untreated treated] _]
   (/ (- untreated treated) untreated)))
