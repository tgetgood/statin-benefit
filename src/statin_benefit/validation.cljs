(ns statin-benefit.validation)

(def hard-limits
  {:total-c     {:max 12.5}
   :ldl-c       {:max 6}
   :hdl-c       {:min 0.5 :max 3}
   :age         {:min 18 :max 100}
   :bp-systolic {:min 70 :max 250}})

(def soft-limits
  {:bp-systolic {:min 90 :max 180}
   :age         {:max 80}})

(defn in-range? [x {:keys [min max]}]
  (cond
    (and (nil? min) (nil? max)) true
    (and min max)               (<= min x max)
    min                         (<= min x)
    :else                       (<= x max)))

(defn non-ideal? [[k v]]
  (when (contains? soft-limits k)
    (not (in-range? v (get soft-limits k)))))

(defn unusable? [[k v]]
  (when (contains? hard-limits k)
    (not (in-range? v (get hard-limits k)))))

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
