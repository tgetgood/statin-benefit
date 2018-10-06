(ns statin-benefit.views
  (:require [re-frame.core :as re-frame]
            [statin-benefit.events :as ev]
            [statin-benefit.subs :as subs]))

(defn percentage [x]
  [:span (str (.toFixed (* 100 x) 2) "%")])

(defn pass-off [k]
  (fn [ev]
    (re-frame/dispatch [k (-> ev .-target .-value)])))

(defn dsub [k]
  (percentage
   @(re-frame/subscribe [k])))

(defn main-panel []
  [:div.container
   [:div.row
    [:div.columns.two
     [:label {:for "age"} "Age"]
     [:input#age.u-full-width
      {:type :number
       :min 1
       :step 1
       :max 120
       :placeholder 50
       :on-change (pass-off ::ev/age)}]]

    [:div.columns.six.fix-offset-by-two
     [:label {:for "sex"} "Sex"]
     [:div#sex.row
      [:span
       [:input {:type :radio :name :sex :value :male
                :on-change (pass-off ::ev/sex)}]
       " Male"]
      [:span.spacing " "]
      [:span
       [:input {:type :radio :name :sex :value :female
                 :on-change (pass-off ::ev/sex)}]
       " Female"]]]]

   [:div.row
    [:div.columns.four
     [:label {:for "ethnicity"} "Ethnicity"]
     [:select#ethnicity {:on-change (pass-off ::ev/ethnicity)
                         :default-value :none}
      [:option {:disabled true :value :none} "--- Select ---"]
      [:option {:value :black} "African American"]
      [:option {:value :white} "White"]]]]

   [:div.vspacer]

   [:div.row
    [:div.columns.four
     [:label {:for "bp"} "Blood Pressure"]
     [:div#bp.row
      [:div.columns.five
       [:input.u-full-width {:type :number :min 0 :placeholder 120
                             :on-change (pass-off ::ev/bp-systolic)}]]
      [:div.columns.one
       [:h2 " / "]]
      [:div.columns.five
       [:input.u-full-width {:type :number :min 0 :placeholder 80
                             :on-change (pass-off ::ev/bp-diastolic)}]]]]

    [:div.columns.eight
     [:label {:for "bp-treatment"} "Currently being treated for hypertension?"]
     [:div#bp-treatment.row
      [:input {:type :radio :name :hypertension :value 1
                :on-change (pass-off ::ev/hypertension)}]
      " Yes"
      [:span.spacing " "]
      [:input {:type :radio :name :hypertension :value 0
                :on-change (pass-off ::ev/hypertension)}]
      " No"]]]

   [:div.row
    [:label {:for "cholesterol"} "Cholesterol:"]
    [:div#cholesterol.row
     [:div.columns.three
      [:label {:for "total"} "Total"]
      [:input#total.u-full-width {:type :number :min 0
                                   :on-change (pass-off ::ev/total-c)}]]
     [:div.columns.three
      [:label {:for "ldl"} "LDL"]
      [:input#ldl.u-full-width {:type :number :min 0
                                 :on-change (pass-off ::ev/ldl-c)}]]
     [:div.columns.three
      [:label {:for "hdl"} "HDL"]
      [:input#hdl.u-full-width {:type :number :min 0
                                 :on-change (pass-off ::ev/hdl-c)}]]
     [:div.columns.three
      [:label {:for "units"} "Units"]
      [:select#units {:on-change (pass-off ::ev/c-units)
                      :default-value :mg-dl}
       [:option {:value :mmol-l} "mmol/L"]
       [:option {:value :mg-dl} "mg/dL"]]]]]

   [:div.row
    [:div.columns.four
     [:label {:for "smoker"} "Do you currently smoke?"]
     [:div#smoker.row
      [:input {:type :radio :name :smoker? :value 1
               :on-change (pass-off ::ev/smoker?)}]
      " Yes"
      [:span.spacing " "]
      [:input {:type :radio :name :smoker? :value 0
               :on-change (pass-off ::ev/smoker?)}]
      " No"]]

    [:div.columns.four
     [:label {:for "diabetic"} "Are you diabetic?"]
     [:div#diabetic.row
      [:input {:type :radio :name :diabetic :value 1
               :on-change (pass-off ::ev/diabetic?)}]
      " Yes"
      [:span.spacing " "]
      [:input {:type :radio :name :diabetic :value 0
               :on-change (pass-off ::ev/diabetic?)}]
      " No"]]]

   [:div.row
    [:div.columns.six
     [:label {:for "statins"} "Statin Treatement Intensity"]
     [:select#statins {:default-value :moderate
                       :on-change (pass-off ::ev/intensity)}
      [:option {:value :low} "Low"]
      [:option {:value :moderate} "Moderate"]
      [:option {:value :high} "High"]]]]

   [:div.vspacer]

   [:div.row
    [:h4 "Results"]]

   [:div.row
    [:div.columns.four
     [:span "Untreated survival: " (dsub ::subs/untreated-survival)]]
    [:div.columns.four
     [:span "Untreated risk: " (dsub ::subs/untreated-risk)]]]
   [:div.row
    [:div.columns.four
     [:span "Treated survival: " (dsub ::subs/treated-survival)]]
    [:div.columns.four
     [:span "Treated risk: " (dsub ::subs/treated-risk)]]]

   [:div.row
    [:div.columns.four
     [:span "Risk Reduction: " (dsub ::subs/reduction)]]
    [:div.columns.four
     [:span "Relative Risk Reduction: " (dsub ::subs/reduction-percentage)]]]
   ]
  )
