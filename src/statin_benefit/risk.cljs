(ns statin-benefit.risk
  (:require re-frame.db
            [statin-benefit.math :refer [ln exp mmol->mg mg->mmol]]))

(defn cholesterol-conversion [x units]
  (* x (if (= units :mg-dl) 1 mmol->mg)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Parameter functions for the Pooled Cohort Risk
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ln-age [{:keys [age]}]
  (ln age))

(defn ln-age**2 [stats]
  (let [x (ln-age stats)]
    (* x x)))

(defn ln-total-c [{:keys [total-c c-units]}]
  (ln (cholesterol-conversion total-c c-units)))

(defn ln-age*ln-total-c [stats]
  (* (ln-age stats) (ln-total-c stats)))

(defn ln-hdl-c [{:keys [hdl-c c-units]}]
  (ln (cholesterol-conversion hdl-c c-units)))

(defn ln-age*ln-hdl-c [stats]
  (* (ln-age stats) (ln-hdl-c stats)))

(defn treated-bp? [{:keys [hypertension]}]
  (if hypertension 1 0))

(defn ln-bp [{:keys [bp-systolic]}]
  (ln bp-systolic))

(defn ln-treated-bp [stats]
  (* (treated-bp? stats) (ln-bp stats)))

(defn ln-untreated-bp [stats]
  (* (- 1 (treated-bp?)) (ln-bp stats)))

(defn ln-age*ln-treated-bp [stats]
  (* (ln-age stats) (ln-treated-bp stats)))

(defn ln-age*ln-untreated-bp [stats]
  (* (ln-age stats) (ln-untreated-bp stats)))

(defn male? [{:keys [sex]}]
  (if (= sex :male) 1 0))

(defn smoker? [{:keys [smoker?]}]
  (if smoker? 1 0))

(defn ln-age*smoker? [stats]
  (* (smoker? stats) (ln-age stats)))

(defn diabetic? [{:keys [diabetic?]}]
  (if diabetic? 1 0))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Parameter table for Pooled Cohort Risk
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ten-year-risk
  {{:ethnicity :white :sex :female}
   {:baseline-survival 0.9665
    :mean-score        -29.18
    :parameters        {ln-age            -29.799
                        ln-age**2         4.884
                        ln-total-c        13.540
                        ln-age*ln-total-c -3.114
                        ln-hdl-c          -13.578
                        ln-age*ln-hdl-c   3.149
                        ln-treated-bp     2.019
                        ln-untreated-bp   1.957
                        smoker?           7.574
                        ln-age*smoker?    -1.665
                        diabetic?         0.661}}

   {:ethnicity :black :sex :female}
   {:baseline-survival 0.9533
    :mean-score        86.61
    :parameters        {ln-age                 17.114
                        ln-total-c             0.940
                        ln-hdl-c               -18.920
                        ln-age*ln-hdl-c        4.475
                        ln-treated-bp          29.291
                        ln-age*ln-treated-bp   -6.432
                        ln-untreated-bp        27.820
                        ln-age*ln-untreated-bp -6.087
                        smoker?                0.691
                        diabetic?              0.874}}

   {:ethnicity :white :sex :male}
   {:baseline-survival 0.9144
    :mean-score        61.18
    :parameters        {ln-age            12.344
                        ln-total-c        11.853
                        ln-age*ln-total-c -2.664
                        ln-hdl-c          -7.990
                        ln-age*ln-hdl-c   1.769
                        ln-treated-bp     1.797
                        ln-untreated-bp   1.764
                        smoker?           7.837
                        ln-age*smoker?    -1.795
                        diabetic?         0.658}}

   {:ethnicity :black :sex :male}
   {:baseline-survival 0.8954
    :mean-score        19.54
    :parameters        {ln-age          2.469
                        ln-total-c      0.302
                        ln-hdl-c        -0.307
                        ln-treated-bp   1.916
                        ln-untreated-bp 1.809
                        smoker?         0.549
                        diabetic?       0.645}}})


(defn individual-sum [parameters stats]
  (transduce (map (fn [[f c]] (* c (f stats)))) + parameters))

(defn untreated-survival [stats]
  (let [{:keys [baseline-survival mean-score parameters]}
        (get ten-year-risk (select-keys stats [:ethnicity :sex]))

        score (individual-sum parameters stats)]
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
