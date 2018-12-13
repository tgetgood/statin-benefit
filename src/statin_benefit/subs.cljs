(ns statin-benefit.subs
  (:require [re-frame.core :as re-frame]
            [statin-benefit.risk.common :as rc]
            [statin-benefit.risk.ten :as risk]
            [statin-benefit.risk.thirty :as risk30]
            [statin-benefit.validation :as validation]))

;;;;; Form

(run! (fn [ev]
        (re-frame/reg-sub
         (keyword (namespace ::x) ev)
         (fn [db]
           (get db ev))))
      (keys validation/fields))

(re-frame/reg-sub
 ::filled?
 (fn [db]
   (validation/form-valid? db)))

(re-frame/reg-sub
 ::validation
 (fn [db [_ k]]
   (let [units (:c-units db)
         v (get db k)]
     (cond
       (nil? v) " incomplete"
       (validation/unusable? units [k v]) " invalid"
       (not (validation/valid? v)) " invalid"
       (validation/non-ideal? [k v]) " field-warning"))))

(re-frame/reg-sub
 ::db
 (fn [db]
   db))

(re-frame/reg-sub
 ::units
 (fn [db]
   (:c-units db)))

;;;;; 10 year risk

(re-frame/reg-sub
 ::current-ten-year-risk
 :<- [::db]
 :<- [::currently-on-statins?]
 (fn [[db c?]]
   (if c?
     (- 1 (risk/treated-survival db (rc/ldl-reduction db true)))
     (- 1 (risk/untreated-survival db)))))

(re-frame/reg-sub
 ::treated-ten-year-risk
 (fn [db]
   (- 1 (risk/treated-survival db (rc/ldl-reduction db)))))

(re-frame/reg-sub
 ::number-to-treat-ten-years
 :<- [::treated-ten-year-risk]
 :<- [::current-ten-year-risk]
 (fn [[treated untreated] _]
   (/ 1 (- untreated treated))))

(re-frame/reg-sub
 ::ten-year-risk-reduction-percentage
 :<- [::current-ten-year-risk]
 :<- [::treated-ten-year-risk]
 (fn [[untreated treated] _]
   (/ (- untreated treated) untreated)))

;;;;; 30 year risk

(re-frame/reg-sub
 ::current-thirty-year-risk
 :<- [::db]
 :<- [::currently-on-statins?]
 (fn [[db c?]]
   (if c?
     (* (risk30/untreated-risk db)
        (- 1 (risk30/risk-reduction-factor db (rc/ldl-reduction db true))))
     (risk30/untreated-risk db))))

(re-frame/reg-sub
 ::treated-thirty-year-risk
 (fn [db]
   (* (risk30/untreated-risk db)
      (- 1 (risk30/risk-reduction-factor db (rc/ldl-reduction db false))))))

(re-frame/reg-sub
 ::number-to-treat-thirty-years
 :<- [::treated-thirty-year-risk]
 :<- [::current-thirty-year-risk]
 (fn [[treated untreated] _]
   (/ 1 (- untreated treated))))

(re-frame/reg-sub
 ::thirty-year-risk-reduction-percentage
  :<- [::current-thirty-year-risk]
  :<- [::treated-thirty-year-risk]
 (fn [[current treated] _]
   (/ (- current treated) current)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Validation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 ::non-negative-benefit?
 :<- [::number-to-treat-ten-years]
 :<- [::number-to-treat-thirty-years]
 (fn [ntts _]
   (and
    (every? pos? ntts)
    (not-any? infinite? ntts))))

(re-frame/reg-sub
 ::positive-benefit?
 :<- [::number-to-treat-ten-years]
 :<- [::number-to-treat-thirty-years]
 (fn [ntts _]
   (every? pos? ntts)))

(re-frame/reg-sub
 ::warning
 :<- [::db]
 :<- [::positive-benefit?]
 (fn [[db p?] _]
   (when-not (and (= (:current-ezetimibe? db) (:target-ezetimibe? db))
                  (= (:current-intensity db) (:target-intensity db)))
     (cond
       (some validation/non-ideal? db)
       (str "warning: some parameters are outside the intended range of the"
            " model. the results might not be reliable.")

       (not p?) "warning: ASCVD risk increases under the proposed change."

       (< (:age db) 40)
       "10 year risk calculation isn't applicable to people under 40."))))

(re-frame/reg-sub
 ::danger
 (fn [db]
   (some (partial validation/unusable? (:c-units db)) db)))
