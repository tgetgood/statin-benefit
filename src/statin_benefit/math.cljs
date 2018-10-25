(ns statin-benefit.math)

(defn ln [x]
  (js/Math.log x))

(defn exp
  ([x]
   (js/Math.exp x))
  ([base power]
   (js/Math.pow base power)))

(def mmol->mg
  38.66976)

(def mg->mmol
  0.02586)
