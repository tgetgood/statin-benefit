(ns statin-benefit.risk)

(def mmol->mg
  38.66976)

(defn cholesterol-conversion [x units]
  (* x (if (= units :mg-dl) 1 mmol->mg)))

(defn ln [x]
  (js/Math.log x))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Factors for the Pooled Cohort Risk
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

(defn ln-treated-bp [{:keys [hypertension bp-systolic]}]
  (if hypertension
    (ln bp-systolic)
    0))

(defn ln-untreated-bp [{:keys [hypertension bp-systolic]}]
  (if-not hypertension
    (ln bp-systolic)
    0))

(defn ln-age*ln-treated-bp [stats]
  (* (ln-age stats) (ln-treated-bp stats)))

(defn ln-age*ln-untreated-bp [stats]
  (* (ln-age stats) (ln-untreated-bp stats)))

(defn smoker? [{:keys [smoker?]}]
  (if smoker? 1 0))

(defn ln-age*smoker? [stats]
  (* (smoker? stats) (ln-age stats)))

(defn diabetic? [{:keys [diabetic?]}]
  (if diabetic? 1 0))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Coefficient table for Pooled Cohort Risk
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ten-year-risk
  {{:ethnicity :white :sex :female}
   {:baseline-survival 0.9665
    :mean-score        -29.18
    :coefficients      {ln-age            -29.799
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
    :coefficients      {ln-age                 17.114
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
    :coefficients      {ln-age            12.344
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
    :coefficients      {ln-age          2.469
                        ln-total-c      0.302
                        ln-hdl-c        -0.307
                        ln-treated-bp   1.916
                        ln-untreated-bp 1.809
                        smoker?         0.549
                        diabetic?       0.645}}})
