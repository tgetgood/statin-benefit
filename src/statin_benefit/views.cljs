(ns statin-benefit.views
  (:require [re-frame.core :as re-frame]
            [statin-benefit.events :as ev]
            [statin-benefit.subs :as subs]
            [statin-benefit.translation :as translation :refer [t]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Subscription wrappers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn statin-dosing []
  [:div.row
   [:div.columns.six
    [:label {:for "intensity"} (t "Statin Treatment Intensity")]
    [:select#intensity {:default-value :none
                        :class           (validation :intensity)
                        :on-change       (pass-off ::ev/intensity)}
     [:option {:value "" :disabled true} "--- " (t "Select") " ---"]
     [:option {:value :low} (t "Low")]
     [:option {:value :moderate} (t "Moderate")]
     [:option {:value :high} (t "High")]]]])

(defn form []
  [:div
   [:div.row
    [:div.columns.three
     [:label {:for "age"} (t "Age")]
     [:input#age.u-full-width
      {:type        :number
       :min         1
       :step        1
       :max         120
       :pattern     "[1-9][0-9]{1,2}"
       :class       (validation :age)
       :placeholder 50
       :on-change   (pass-off ::ev/age)}]]

    [:div.columns.four
     [:label {:for "ethnicity"} (t "Ethnicity")]
     [:select#ethnicity.u-full-width {:on-change     (pass-off ::ev/ethnicity)
                                      :class         (validation :ethnicity)
                                      :default-value :none}
      [:option {:disabled true :value :none} "--- " (t "Select") " ---"]
      [:option {:value :black} (t "African American")]
      [:option {:value :white} (t "White")]]]

    [:div.columns.five
     [:label {:for   "sex"
              :class (validation :sex)}
      (t "Sex")]
     [:div#sex.row.u-full-width
      [:span
       [:input {:type      :radio :name :sex :value :male
                :on-change (pass-off ::ev/sex)}]
       [:span " "]
       (t "Male")]
      [:span.hspacer " "]
      [:span
       [:input {:type       :radio :name :sex :value :female
                :on-change (pass-off ::ev/sex)}]
       [:span " "]
       (t "Female")]]]]

   [:div.vspacer]

   [:div.row
    [:div.columns.five
     [:label {:for "bp"} (t "Blood Pressure")]
     [:div#bp
      [:span
       [:input#bp-systolic.bp-input
        {:type        :number
         :min         0
         :class       (validation :bp-systolic)
         :placeholder 120
         :pattern     "[1-9][0-9]{1,2}"
         :on-change   (pass-off ::ev/bp-systolic)}]]
      [:span.slash " / "]
      [:span
       [:input#bp-diastolic.bp-input
        {:type        :number
         :min         0
         :placeholder 80
         :pattern     "[1-9][0-9]{1,2}"
         :on-change   (pass-off ::ev/bp-diastolic)}]]]]

    [:div.columns.seven
     [:label {:for   "bp-treatment"
              :class (validation :hypertension)}
      (t "Are you currently being treated for hypertension?")]
     [:div#bp-treatment.row
      [:input {:type       :radio :name :hypertension :value 1
               :on-change (pass-off ::ev/hypertension)}]
      [:span " "]
      (t "Yes")
      [:span.hspacer " "]
      [:input {:type       :radio :name :hypertension :value 0
               :on-change (pass-off ::ev/hypertension)}]
      [:span " "]
      (t "No")]]]

   [:div.vspacer]

   [:div.row
    [:label {:for "cholesterol"} (t "Cholesterol") ":"]
    [:div#cholesterol.row
     [:div.columns.three
      [:label {:for "total-c"} (t "Total")]
      [:input#total-c.u-full-width {:type     :number :min 0
                                    :class      (validation :total-c)
                                    :on-change (pass-off ::ev/total-c)}]]
     [:div.columns.three
      [:label {:for "ldl-c"} "LDL"]
      [:input#ldl-c.u-full-width {:type    :number :min 0
                                  :class     (validation :ldl-c)
                                  :on-change (pass-off ::ev/ldl-c)}]]
     [:div.columns.three
      [:label {:for "hdl-c"} "HDL"]
      [:input#hdl-c.u-full-width {:type     :number :min 0
                                  :class      (validation :hdl-c)
                                  :on-change (pass-off ::ev/hdl-c)}]]
     [:div.columns.three
      [:label {:for "c-units"} (t "Units")]
      [:select#c-units {:on-change   (pass-off ::ev/c-units)
                        :default-value :mmol-l}
       [:option {:value :mmol-l} "mmol/L"]
       [:option {:value :mg-dl} "mg/dL"]]]]]

   [:div.vspacer]

   [:div.row
    [:div.columns.six
     [:label {:for   "smoker"
              :class (validation :smoker?)}
      (t "Do you currently smoke?")]
     [:div#smoker.row
      [:input {:type      :radio :name :smoker? :value 1
               :on-change (pass-off ::ev/smoker?)}]
      [:span " "]
      (t "Yes")
      [:span.hspacer " "]
      [:input {:type      :radio :name :smoker? :value 0
               :on-change (pass-off ::ev/smoker?)}]
      [:span " "]
      (t "No")]]

    [:div.columns.six
     [:label {:for   "diabetic"
              :class (validation :diabetic?)}
      (t "Are you diabetic?")]
     [:div#diabetic.row
      [:input {:type      :radio :name :diabetic? :value 1
               :on-change (pass-off ::ev/diabetic?)}]
      [:span " "]
      (t "Yes")
      [:span.hspacer " "]
      [:input {:type      :radio :name :diabetic? :value 0
               :on-change (pass-off ::ev/diabetic?)}]
      [:span " "]
      (t "No")]]]

   [:div.vspacer]

   [statin-dosing]])

(defn results []
  [:div
   [:div.row
    [:h4 (t "Results")]
    (if @(re-frame/subscribe [::subs/filled?])
      [:div
       [:div.row
        [:table.u-full-width
         [:thead
          [:tr
           [:th ""]
           [:th [:strong (t "10 Year ASCVD Risk")]]
           [:th [:strong (t "30 Year ASCVD Risk")]]]]
         [:tbody
          [:tr
           [:td [:strong (t "Without Statins")]]
           [:td (dsub ::subs/untreated-risk)]
           [:td "?"]]
          [:tr
           [:td [:strong (t "With Statins")]]
           [:td (dsub ::subs/treated-risk)]
           [:td "?"]]
          [:tr
           [:td [:strong (t "Benefit of Statin Therapy")]]
           [:td (dsub ::subs/risk-reduction)]
           [:td "?"]]
          [:tr
           [:td [:strong (t "Risk Reduction Factor")]]
           [:td (dsub ::subs/risk-reduction-percentage)]
           [:td "?"]]]]]]
      [:div (t "Fill in the form to see your results.")])]])

(defn language-switch []
  (let [[text switch-to] (translation/switcher)]
    [:a {:on-click #(re-frame/dispatch [::ev/change-language switch-to])}
     text]))

(defn copyright []
  ;; TODO: figure out who
  (str '\u00A9 " " (t "The authors") ". " (t "Released under the LGPL-3.0")))

(defn links []
  [:div.row
   [:div.columns.six [copyright]]
   [:div.columns.three
    [:a {:href "https://github.com/tgetgood/statin-benefit#references"}
     (t "References")]]
   [:div.columns.three
    [:a {:href "https://github.com/tgetgood/statin-benefit"}
     (t "Source Code")]]])

(defn title-bar []
  [:div
   [:h3 (t "Personalised Statin Benefit Calculator")]
   [:div.u-pull-right [language-switch]]])

(defn main-panel []
  [:div.container
   [title-bar]
   [:hr]
   [form]
   [:hr]
   [results]
   [:hr]
   [links]
   [:hr]])
