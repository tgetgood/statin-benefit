(ns statin-benefit.risk.common
  (:require [statin-benefit.math :refer [ln mg->mmol mmol->mg]]))

(defn cholesterol-conversion [x units]
  (* x (if (= units :mg-dl) 1 mmol->mg)))

;;;;; Parameter fns

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
  (* (- 1 (treated-bp? stats)) (ln-bp stats)))

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

;;;;; Helpers

(defn individual-sum [parameters stats]
  (transduce (map (fn [[f c]] (* c (f stats)))) + parameters))

(def intensity-table
  {:low      0.2
   :moderate 0.4
   :high     0.6})

(defn intensity [{:keys [intensity ezetimibe?]}]
  (let [base (get intensity-table intensity)]
   (if ezetimibe?
     (+ base 0.1)
     base)))

(defn ldl-reduction [{:keys [ldl-c c-units] :as stats}]
  (* ldl-c (if (= c-units :mmol-l) 1 mg->mmol) (intensity stats)))
