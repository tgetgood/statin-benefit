(ns statin-benefit.views
  (:require [re-frame.core :as re-frame]
            [statin-benefit.events :as ev]
            [statin-benefit.config :as config]
            [statin-benefit.subs :as subs]
            [statin-benefit.translation :as translation :refer [t]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Subscription wrappers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn percentage [x]
  [:span (str (.toFixed (* 100 x) 2) "%")])

(defn events-key [k]
  (keyword :statin-benefit.events (name k)))

(defn pass-off [k]
  (fn [ev]
    (re-frame/dispatch [(events-key k) (-> ev .-target .-value)])))

(defn validation [k]
  @(re-frame/subscribe [::subs/validation k]))

(defn percent-sub [k]
  (percentage
   @(re-frame/subscribe [k])))

(defn num-sub [k]
  [:span (str (.toFixed @(re-frame/subscribe [k]) 0))])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn yes-no-radio
  "Standard yes/no radio button."
  [k question]
  [:div
     [:label {:for   (name k)
              :class (validation k)}
      question]
     [:div {:id (name k) :class "row"}
      [:input {:type      :radio :name k :value 1
               :on-change (pass-off k)}]
      [:span " "]
      (t "Yes")
      [:span.hspacer " "]
      [:input {:type      :radio :name k :value 0
               :on-change (pass-off k)}]
      [:span " "]
      (t "No")]])

(defn number-box
  "Numerical input box."
  [k label]
  [:div
   [:label {:for (name k)} label]
   [:input.u-full-width {:id        (name k)
            :type      :number :min 0
            :class     (validation k)
            :on-change (pass-off k)}]])

(defn select
  "Standard HTML select"
  [k label opts & [default]]
  (let [options (map (fn [[k v]] [:option {:value k} v]) opts)]
    [:div
     [:label {:for (name k)} label]
     (into [:select {:id (name k)
                     :on-change   (pass-off k)
                     :default-value (or default :none)}]
           (if default
             options
             (cons
              [:option {:disabled true :value :none} "--- " (t "Select") " ---"]
              options)))]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Main View
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn statin-dosing []
  [:div.row
   [:div.columns.six
    [select :intensity (t "Statin Treatment Intensity")
     {:low (t "Low")
      :moderate (t "Moderate")
      :high (t "High")}]]])

(defn cholesterol []
  [:div
   [:div.row
    [yes-no-radio :currently-treated? (t "Are you currently taking statins?")]]
   [:div.vspacer]

   [:div.row
    [:label {:for "cholesterol"} (t "Pretreatment Cholesterol") ":"]
    [:div#cholesterol.row
     [:div.columns.three [number-box :total-c (t "Total")]]
     [:div.columns.three [number-box :ldl-c "LDL-C"]]
     [:div.columns.three [number-box :hdl-c "HDL-C"]]
     [:div.columns.three
      [select :c-units (t "Units") {:mmol-l "mmol/L" :mg-dl "mg/dL"} :mmol-l]]]]

   [:div.vspacer]

   [statin-dosing]])

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
       :class       (validation :age)
       :placeholder 50
       :on-change   (pass-off :age)}]]

    [:div.columns.four
     [select :ethnicity  (t "Ethnicity")
      {:black (t "African American")
       :white (t "White")}]]

    [:div.columns.five
     [:label {:for   "sex"
              :class (validation :sex)}
      (t "Sex")]
     [:div#sex.row.u-full-width
      [:span
       [:input {:type      :radio :name :sex :value :male
                :on-change (pass-off :sex)}]
       [:span " "]
       (t "Male")]
      [:span.hspacer " "]
      [:span
       [:input {:type       :radio :name :sex :value :female
                :on-change (pass-off :sex)}]
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
         :on-change   (pass-off :bp-systolic)}]]
      [:span.slash " / "]
      [:span
       [:input#bp-diastolic.bp-input
        {:type        :number
         :min         0
         :placeholder 80
         :on-change   (pass-off :bp-diastolic)}]]]]

    [:div.columns.seven
     [yes-no-radio :hypertension
      (t "Are you currently being treated for hypertension?")]]]

   [:div.vspacer]

   [:div.row
    [:div.columns.six
     [yes-no-radio :smoker? (t "Do you currently smoke?")]]
    [:div.columns.six
     [yes-no-radio :diabetic? (t "Are you diabetic?")]]]

   [:div.vspacer]

   [cholesterol]])

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
           [:td (percent-sub ::subs/untreated-risk)]
           [:td "?"]]
          [:tr
           [:td [:strong (t "With Statins")]]
           [:td (percent-sub ::subs/treated-risk)]
           [:td "?"]]
          [:tr
           [:td [:strong (t "Number to Treat to Prevent 1 Event")]]
           [:td (num-sub ::subs/number-to-treat)]

           [:td "?"]]
          [:tr
           [:td [:strong (t "Risk Reduction Factor")]]
           [:td (percent-sub ::subs/risk-reduction-percentage)]
           [:td "?"]]]]]]
      [:div (t "Fill in the form to see your results.")])]])

(defn language-switch []
  (if-let [lang (translation/current)]
    (let [[text switch-to] (translation/switcher lang)]
      [:a {:on-click #(re-frame/dispatch [:ev/change-language switch-to])}
       text])
    (let [[text switch-to] (translation/switcher config/startup-lang)]
      [:a {:href (if (= switch-to :en)
                   "../index.html"
                   "fr/index.html")}
       text])))

(defn copyright []
  [:div
   [:p (str '\u00A9
            "2018 George"'\u00A0 "Thanassoulis,"
            " " "Michael" '\u00A0 "J." '\u00A0 "Pencina,"
            " & " "Allan" '\u00A0 "D." '\u00A0 "Sniderman"
            ". ")]
   [:p (str (t "Released under the") " LGPL" '\u2011 "3.0")]])

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
   [:h3 (t "MUHC-Duke Statin Benefit Calculator")]
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
