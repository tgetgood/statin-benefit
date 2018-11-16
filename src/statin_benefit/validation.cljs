(ns statin-benefit.validation)

(def hard-limits
  {:total-c #(<= 0 % 12.5)
   :ldl-c #(<= 0 % 6)
   :hdl-c #(<= 0.5 % 3)
   :age #(<= 18 % 100)
   :bp-systolic #(<= 70 % 250)})

(def soft-limits
  {:bp-systolic #(<= 90 % 180)
   :age #(<= % 80)})

(defn non-ideal? [[k v]]
  (when (contains? soft-limits k)
    (not ((get soft-limits k) v))))

(defn unusable? [[k v]]
  (when (contains? hard-limits k)
    (not ((get hard-limits k) v))))

(defn number [x]
  (js/parseFloat x))

(defn bool [x]
  (= "1" x))

(defn check [x]
  x)

(def fields
  {:age                   number
   :sex                   keyword
   :ethnicity             keyword
   :bp-systolic           number
   :bp-diastolic          number
   :hypertension          bool
   :total-c               number
   :ldl-c                 number
   :hdl-c                 number
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
     (not= :zero (:target-intensity form)))))
