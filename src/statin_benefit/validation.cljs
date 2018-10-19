(ns statin-benefit.validation
  (:refer-clojure :exclude [int]))

(defn int [x]
  (js/parseInt x))

(defn bool [x]
  (= "1" x))

(def fields
  {:age                int
   :sex                keyword
   :ethnicity          keyword
   :bp-systolic        int
   :bp-diastolic       int
   :hypertension       bool
   :total-c            int
   :ldl-c              int
   :hdl-c              int
   :c-units            keyword
   :smoker?            bool
   :diabetic?          bool
   :currently-treated? bool
   :intensity          keyword})

(def required-keys
  (->> (dissoc fields :bp-diastolic)
       keys
       (map name)
       (map keyword)))

(defn valid? [x]
  (and (not (nil? x))
       (or (not (number? x)) (not (js/isNaN x)))))
