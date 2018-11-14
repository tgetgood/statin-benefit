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
   (- 1 (risk/treated-survival db (rc/ldl-reduction db)))))

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
 :<- [::untreated-thirty-year-risk]
 :<- [::thirty-year-risk-reduction-percentage]
 (fn [[risk rrf] _]
   (* risk (- 1 rrf))))

(re-frame/reg-sub
 ::number-to-treat-thirty-years
 :<- [::treated-thirty-year-risk]
 :<- [::untreated-thirty-year-risk]
 (fn [[treated untreated] _]
   (/ 1 (- untreated treated))))

(re-frame/reg-sub
 ::thirty-year-risk-reduction-percentage
 (fn [db]
   (risk30/risk-reduction-factor db (rc/ldl-reduction db))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Relative dosages
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 ::current-ten-year-risk
 (fn [db]
   (- 1 (risk/treated-survival db (rc/ldl-reduction db true)))))

(re-frame/reg-sub
 ::current-thirty-year-risk
 (fn [db]
   (* (risk30/untreated-risk db)
      (- 1 (risk30/risk-reduction-factor db (rc/ldl-reduction db true))))))

(re-frame/reg-sub
 ::rel-num-to-treat-ten
 :<- [::treated-ten-year-risk]
 :<- [::current-ten-year-risk]
 (fn [[treated current] _]
   (when-not (= current treated)
     (/ 1 (- current treated)))))

(re-frame/reg-sub
 ::rel-num-to-treat-thirty
 :<- [::treated-thirty-year-risk]
 :<- [::current-thirty-year-risk]
 (fn [[target current] _]
   (when-not (= current target)
     (/ 1 (- current target)))))

(re-frame/reg-sub
 ::rel-risk-reduction-ten
  :<- [::current-ten-year-risk]
  :<- [::treated-ten-year-risk]
 (fn [[current treated] _]
   (/ (- current treated) current)))

(re-frame/reg-sub
 ::rel-risk-reduction-thirty
  :<- [::current-thirty-year-risk]
  :<- [::treated-thirty-year-risk]
 (fn [[current treated] _]
   (/ (- current treated) current)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Validation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 ::positive-benefit?
 :<- [::rel-num-to-treat-ten]
 :<- [::rel-num-to-treat-thirty]
 (fn [ntts _]
   (every? pos? ntts )))

(re-frame/reg-sub
 ::warning
 :<- [::positive-benefit?]
 :<- [::current-intensity]
 :<- [::target-intensity]
 :<- [::current-ezetimibe?]
 :<- [::target-ezetimibe?]
 (fn [[p? ci ti ce te] _]
   (cond
     (and (= ce te) (= ci ti)) false
     (not p?) "warning: ASCVD risk increases under the proposed change!")))

(re-frame/reg-sub
 ::status
 (fn [db _]
   :good))
