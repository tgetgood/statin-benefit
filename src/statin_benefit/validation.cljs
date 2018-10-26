(ns statin-benefit.validation
  (:refer-clojure :exclude [int]))

(defn int [x]
  (js/parseInt x))

(defn bool [x]
  (= "1" x))

(defn check [x]
  x)

(def fields
  {:age                   int
   :sex                   keyword
   :ethnicity             keyword
   :bp-systolic           int
   :bp-diastolic          int
   :hypertension          bool
   :total-c               int
   :ldl-c                 int
   :hdl-c                 int
   :c-units               keyword
   :smoker?               bool
   :diabetic?             bool
   :currently-on-statins? bool
   :current-ezetimibe?    check
   :current-intensity     keyword
   :target-ezetimibe?     check
   :target-intensity      keyword
   :lang                  keyword})

(def optional
  [:bp-diastolic
   :currently-on-statins?
   :lang
   :current-ezetimibe?
   :current-intensity
   :target-ezetimibe?])

(def required-keys
  (->> (apply dissoc fields optional)
       keys
       (map name)
       (map keyword)))

(defn valid? [x]
  (and (not (nil? x))
       (or (not (number? x)) (not (js/isNaN x)))))

(defn form-valid? [form]
  (and
   (every? #(valid? (get form %)) required-keys)
   (if (:currently-on-statins? form)
     (valid? (:current-intensity form))
     true)))
