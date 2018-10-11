(ns statin-benefit.views
  (:require [re-frame.core :as re-frame]
            [statin-benefit.events :as ev]
            [statin-benefit.subs :as subs]))

(defn percentage [x]
  [:span (str (.toFixed (* 100 x) 2) "%")])

(defn pass-off [k]
  (fn [ev]
    (re-frame/dispatch [k (-> ev .-target .-value)])))

(defn validation [k]
  @(re-frame/subscribe [::subs/validation k]))

(defn dsub [k]
  (percentage
   @(re-frame/subscribe [k])))

(defn main-panel []
  [:div.container
   [:div.row
    [:div.columns.three
     [:label {:for "age"} "Age"]
     [:input#age.u-full-width
      {:type :number
       :min 1
       :step 1
       :max 120
       :pattern "[1-9][0-9]{1,2}"
       :class (validation :age)
       :placeholder 50
       :on-change (pass-off ::ev/age)}]]

    [:div.columns.four
     [:label {:for "ethnicity"} "Ethnicity"]
     [:select#ethnicity.u-full-width {:on-change (pass-off ::ev/ethnicity)
                                      :class (validation :ethnicity)
                                      :default-value :none}
      [:option {:disabled true :value :none} "--- Select ---"]
      [:option {:value :black} "African American"]
      [:option {:value :white} "White"]]]

    [:div.columns.five
     [:label {:for "sex"
              :class (validation :sex)}
      "Sex"]
     [:div#sex.row.u-full-width
      [:span
       [:input {:type :radio :name :sex :value :male
                :on-change (pass-off ::ev/sex)}]
       " Male"]
      [:span.hspacer " "]
      [:span
       [:input {:type :radio :name :sex :value :female
                 :on-change (pass-off ::ev/sex)}]
       " Female"]]]]

   [:div.vspacer]

   [:div.row
    [:div.columns.five
     [:label {:for "bp"} "Blood Pressure"]
     [:div#bp
      [:span
       [:input.bp-input
        {:type :number
         :min 0
         :class (validation :bp-systolic)
         :placeholder 120
         :pattern "[1-9][0-9]{1,2}"
         :on-change (pass-off ::ev/bp-systolic)}]]
      [:span.slash " / "]
      [:span
       [:input.bp-input
        {:type :number
         :min 0
         :placeholder 80
         :pattern "[1-9][0-9]{1,2}"
         :on-change (pass-off ::ev/bp-diastolic)}]]]]

    [:div.columns.seven
     [:label {:for "bp-treatment"
              :class (validation :hypertension)}
      "Currently being treated for hypertension?"]
     [:div#bp-treatment.row
      [:input {:type :radio :name :hypertension :value 1
                :on-change (pass-off ::ev/hypertension)}]
      " Yes"
      [:span.hspacer " "]
      [:input {:type :radio :name :hypertension :value 0
                :on-change (pass-off ::ev/hypertension)}]
      " No"]]]

   [:div.vspacer]

   [:div.row
    [:label {:for "cholesterol"} "Cholesterol:"]
    [:div#cholesterol.row
     [:div.columns.three
      [:label {:for "total"} "Total"]
      [:input#total.u-full-width {:type :number :min 0
                                  :class (validation :total-c)
                                   :on-change (pass-off ::ev/total-c)}]]
     [:div.columns.three
      [:label {:for "ldl"} "LDL"]
      [:input#ldl.u-full-width {:type :number :min 0
                                :class (validation :ldl-c)
                                :on-change (pass-off ::ev/ldl-c)}]]
     [:div.columns.three
      [:label {:for "hdl"} "HDL"]
      [:input#hdl.u-full-width {:type :number :min 0
                                :class (validation :hdl-c)
                                 :on-change (pass-off ::ev/hdl-c)}]]
     [:div.columns.three
      [:label {:for "units"} "Units"]
      [:select#units {:on-change (pass-off ::ev/c-units)
                      :default-value :mmol-l}
       [:option {:value :mmol-l} "mmol/L"]
       [:option {:value :mg-dl} "mg/dL"]]]]]

   [:div.vspacer]

   [:div.row
    [:div.columns.six
     [:label {:for "smoker"
              :class (validation :smoker?)}
      "Do you currently smoke?"]
     [:div#smoker.row
      [:input {:type :radio :name :smoker? :value 1
               :on-change (pass-off ::ev/smoker?)}]
      " Yes"
      [:span.hspacer " "]
      [:input {:type :radio :name :smoker? :value 0
               :on-change (pass-off ::ev/smoker?)}]
      " No"]]

    [:div.columns.six
     [:label {:for "diabetic"
              :class (validation :diabetic?)}
      "Are you diabetic?"]
     [:div#diabetic.row
      [:input {:type :radio :name :diabetic :value 1
               :on-change (pass-off ::ev/diabetic?)}]
      " Yes"
      [:span.hspacer " "]
      [:input {:type :radio :name :diabetic :value 0
               :on-change (pass-off ::ev/diabetic?)}]
      " No"]]]

   [:div.vspacer]

   [:div.row
    [:div.columns.six
     [:label {:for "statins"} "Statin Treatement Intensity"]
     [:select#statins {:default-value :none
                       :class (validation :intensity)
                       :on-change (pass-off ::ev/intensity)}
      [:option {:value :none :disabled true} "--- Select ---"]
      [:option {:value :low} "Low"]
      [:option {:value :moderate} "Moderate"]
      [:option {:value :high} "High"]]]]

   [:div.vspacer]

   [:div.row
    [:h4 "Results"]]

   (if @(re-frame/subscribe [::subs/filled?])
     [:div
      [:div.row
       [:table.u-full-width
        [:thead
         [:tr
          [:th ""]
          [:th [:strong "Survival"]]
          [:th [:strong "Risk"]]]]
        [:tbody
         [:tr
          [:td [:strong "Without Treatment"]]
          [:td (dsub ::subs/untreated-survival)]
          [:td (dsub ::subs/untreated-risk)]]
         [:tr
          [:td [:strong "With Treatment"]]
          [:td (dsub ::subs/treated-survival)]
          [:td (dsub ::subs/treated-risk)]]]]]
      [:div.vspacer]
      [:div.row
       [:table.u-full-width
        [:thead
         [:tr
          [:td [:strong "Increased likelihood of Survival"]]
          [:td [:strong "Relative Risk Reduction"]]]]
        [:tbody
         [:tr
          [:td (dsub ::subs/risk-reduction)]
          [:td (dsub ::subs/risk-reduction-percentage)]]]]]]
     [:div "Fill in the form to see your results."])])
