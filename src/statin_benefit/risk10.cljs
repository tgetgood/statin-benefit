(ns statin-benefit.risk
  (:require [statin-benefit.math :refer [exp ln mg->mmol]]
            [statin-benefit.risk-fns :as r]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Parameter table for Pooled Cohort Risk
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ten-year-risk
  {{:ethnicity :white :sex :female}
   {:baseline-survival 0.9665
    :mean-score        -29.18
    :parameters        {r/ln-age            -29.799
                        r/ln-age**2         4.884
                        r/ln-total-c        13.540
                        r/ln-age*ln-total-c -3.114
                        r/ln-hdl-c          -13.578
                        r/ln-age*ln-hdl-c   3.149
                        r/ln-treated-bp     2.019
                        r/ln-untreated-bp   1.957
                        r/smoker?           7.574
                        r/ln-age*smoker?    -1.665
                        r/diabetic?         0.661}}

   {:ethnicity :black :sex :female}
   {:baseline-survival 0.9533
    :mean-score        86.61
    :parameters        {r/ln-age                 17.114
                        r/ln-total-c             0.940
                        r/ln-hdl-c               -18.920
                        r/ln-age*ln-hdl-c        4.475
                        r/ln-treated-bp          29.291
                        r/ln-age*ln-treated-bp   -6.432
                        r/ln-untreated-bp        27.820
                        r/ln-age*ln-untreated-bp -6.087
                        r/smoker?                0.691
                        r/diabetic?              0.874}}

   {:ethnicity :white :sex :male}
   {:baseline-survival 0.9144
    :mean-score        61.18
    :parameters        {r/ln-age            12.344
                        r/ln-total-c        11.853
                        r/ln-age*ln-total-c -2.664
                        r/ln-hdl-c          -7.990
                        r/ln-age*ln-hdl-c   1.769
                        r/ln-treated-bp     1.797
                        r/ln-untreated-bp   1.764
                        r/smoker?           7.837
                        r/ln-age*smoker?    -1.795
                        r/diabetic?         0.658}}

   {:ethnicity :black :sex :male}
   {:baseline-survival 0.8954
    :mean-score        19.54
    :parameters        {r/ln-age          2.469
                        r/ln-total-c      0.302
                        r/ln-hdl-c        -0.307
                        r/ln-treated-bp   1.916
                        r/ln-untreated-bp 1.809
                        r/smoker?         0.549
                        r/diabetic?       0.645}}})


(defn untreated-survival [stats]
  (let [{:keys [baseline-survival mean-score parameters]}
        (get ten-year-risk (select-keys stats [:ethnicity :sex]))

        score (r/individual-sum parameters stats)]
    (exp baseline-survival (exp (- score mean-score)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; 10 Year Statin Benefit
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def intensity-table
  {:low      0.2
   :moderate 0.4
   :high     0.6})

(defn ldl-reduction [{:keys [ldl-c c-units intensity]}]
  (* ldl-c (if (= c-units :mmol-l) 1 mg->mmol) (get intensity-table intensity)))

(defn hazard-ration [survival]
  (exp (- (* (ln (- 1 survival)) 0.12346) 0.10821)))

(defn treated-survival [stats]
  (let [us (untreated-survival stats)]
    (exp us
         (exp (hazard-ration us)
              (ldl-reduction stats)))))

(defn test-benefit
  "To compare the calculation to the graphs in the paper."
  [risk ldl]
  (let [untreated-survival (- 1 risk)]
    (- (exp untreated-survival
            (exp (hazard-ration untreated-survival)
                 (ldl-reduction {:ldl-c ldl
                                 :c-units :mg-dl
                                 :intensity :moderate})))
       untreated-survival)))
