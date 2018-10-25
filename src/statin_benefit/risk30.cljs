(ns statin-benefit.risk30
  "Names here refer to the column titles in the Framingham algo spreadsheet
  'cardio30yr_risk_lipids'. If I know what the numbers meant, I'd think up
  better names, but to be frank I may as well be copying gibberish."
  (:require [statin-benefit.math :refer [exp]]
            [statin-benefit.risk :as r]
            [statin-benefit.risk30-constants :as constants]))

(def xbeta
  {:mean 24.728399807068
   :parameters {r/male?       0.55021
                r/ln-age      2.83511
                r/ln-bp       1.99822
                r/ln-total-c  1.4775
                r/ln-hdl-c    -0.86736
                r/smoker?     0.70063
                r/treated-bp? 0.39241
                r/diabetic?   0.9137}})

(def dxbeta
  {:mean       20.5660997877815
   :parameters {r/male?       0.47666
                r/ln-age      3.53291
                r/ln-bp       1.43216
                r/ln-total-c  0.00704
                r/ln-hdl-c    0.09148
                r/smoker?     0.97352
                r/treated-bp? 0.11888
                r/diabetic?   0.45355}})

(defn delta [m stats]
  (exp (- (r/individual-sum (:parameters m) stats) (:mean m))))

(defn untreated-risk [stats]
  (let [delta-x  (delta xbeta stats)
        delta-dx (delta dxbeta stats)]
    (reduce + (map (fn [e j g]
                     (* (exp e delta-x)
                        (exp j delta-dx)
                        delta-x
                        g))
                   constants/E
                   constants/J
                   constants/G))))
