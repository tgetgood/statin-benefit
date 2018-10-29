(ns statin-benefit.views
  (:require
            [re-frame.core :as re-frame]
            [statin-benefit.config :as config]
            [statin-benefit.events :as ev]
            [statin-benefit.subs :as subs]
            [statin-benefit.translation :as translation :refer [t]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Subscription wrappers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn percentage [x]
  [:span (str (.toFixed (* 100 x) 1) "%")])

(defn events-key [k]
  (keyword :statin-benefit.events (name k)))

(defn subs-key [k]
  (keyword :statin-benefit.subs (name k)))

(defn grab [k]
  @(re-frame/subscribe [(subs-key k)]))

(defn pass-off [k]
  (fn [ev]
    (re-frame/dispatch [(events-key k) (-> ev .-target .-value)])))

(defn validation [k]
  @(re-frame/subscribe [::subs/validation k]))

(defn percent-sub [k]
  (percentage
   @(re-frame/subscribe [k])))

(defn num-sub [k]
  [:span (str (.toFixed @(re-frame/subscribe [k]) 1))])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn checkbox
  [k label]
  (let [current (grab k)]
    [:label
     [:input {:id        (name k)
              :on-change #(re-frame/dispatch
                           [(events-key k) (-> %
                                               (unchecked-get "target")
                                               (unchecked-get "checked"))])
              :checked   (true? current)
              :type      :checkbox}]
     [:span.label-body label]]))

(defn yes-no-radio
  "Yes/no radio button."
  [k question]
  (let [current (grab k)]
    [:div
     [:label {:for   (name k)
              :class (validation k)}
      question]
     [:div.row {:id (name k)}
      [:label.columns.three
       [:input {:type      :radio :name k :value 1
                :checked   (true? current)
                :on-change (pass-off k)}]
       [:span.label-body " " (t "Yes")]]
      [:label.columns.three
       [:input {:type      :radio :name k :value 0
                :checked   (false? current)
                :on-change (pass-off k)}]
       [:span.label-body " " (t "No")]]]]))

(defn number-box
  "Numerical input box."
  [k label & [{:keys [placeholder class]}]]
  (let [current (grab k)]
    [:span
     (when label [:label {:for (name k)} label])
     [:input.u-full-width (merge {:id (name k)
                                  :type            :number :min 0
                                  :class           (str (validation k) " " class)
                                  :on-change       (pass-off k)}
                                 (when current
                                   {:default-value current})
                                 (when placeholder
                                   {:placeholder placeholder}))]]))

(defn add-default [options]
  (cons [:option {:disabled true :value :none} "--- " (t "Select") " ---"]
        options))

(defn select
  "Standard HTML select"
  [k label opts]
  (let [options (add-default (map (fn [[k v]] [:option {:value k} v]) opts))
        current (grab k)]
    [:div
     [:label {:for (name k)} label]
     (into [:select {:id (name k)
                     :on-change   (pass-off k)
                     :default-value (if (contains? opts current)
                                      current
                                      :none)}]
           options)]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Main View
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn statin-new-dosing []
  [:div
   [:div.row
    [:div.columns.six
     [select :target-intensity (t "Statin Dosage")
      {:low (t "Low")
       :moderate (t "Moderate")
       :high (t "High")}]]]
   [:div.row
    [checkbox :target-ezetimibe? (t "Plus Ezetimibe")]]])

(defn statin-change-dosing []
  [:div.row
   [:div.columns.four
    [:div
     [select :current-intensity (t "Current Statin Dosage")
      {:low      (t "Low")
       :moderate (t "Moderate")
       :high     (t "High")}]]
    [:div.row
     [checkbox :current-ezetimibe? (t "Plus Ezetimibe")]]]
   [:div.columns.four
    [:div
     [select :target-intensity (t "New Statin Dosage")
      {:zero     (t "None")
       :low      (t "Low")
       :moderate (t "Moderate")
       :high     (t "High")}]]
    [:div.row
     [checkbox :target-ezetimibe? (t "Plus Ezetimibe")]]]])

(defn cholesterol []
  [:div
   [:div.row
    [:label {:for "cholesterol"} (t "Cholesterol") ":"]
    [:div#cholesterol.row
     [:div.columns.three [number-box :total-c (t "Total")]]
     [:div.columns.three [number-box :ldl-c "LDL-C"]]
     [:div.columns.three [number-box :hdl-c "HDL-C"]]
     [:div.columns.three
      [select :c-units (t "Units") {:mmol-l "mmol/L" :mg-dl "mg/dL"} :mmol-l]]]]

   [:div.vspacer]

   [:div.row
    [:div.columns.six
     [yes-no-radio :currently-on-statins? (t "Are you currently taking statins?")]]]

   [:div.vspacer]

   (if @(re-frame/subscribe [::subs/currently-on-statins?])
     [statin-change-dosing]
     [statin-new-dosing])])

(defn form []
  [:div
   [:div.row
    [:div.columns.three
     [number-box :age (t "Age") {:placeholder 50}]]

    [:div.columns.four
     [select :ethnicity  (t "Ethnicity")
      {:black (t "African American")
       :white (t "White")}]]

    [:div.columns.five
     [:label {:for   "sex"
              :class (validation :sex)}
      (t "Sex")]
     [:div#sex.row.u-full-width
      [:label.columns.four
       [:input {:type      :radio :name :sex :value :male
                :on-change (pass-off :sex)}]
       [:span.label-body (t "Male")]]
      [:label.columns.six
       [:input {:type      :radio :name :sex :value :female
                :on-change (pass-off :sex)}]
       [:span.label-body (t "Female")]]]]]

   [:div.vspacer]

   [:div.row
    [:div.columns.five
     [:label {:for "bp"} (t "Blood Pressure")]
     [:div#bp
      [number-box :bp-systolic nil {:placeholder 120
                                    :class "bp-input"}]
      [:span.slash " / "]
      [number-box :bp-diastolic nil {:placeholder 80
                                     :class "bp-input"}]]]

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
           [:td [:strong (t "Without Treatment")]]
           [:td (percent-sub ::subs/untreated-ten-year-risk)]
           [:td (percent-sub ::subs/untreated-thirty-year-risk)]]
          [:tr
           [:td [:strong (t "With Treatment")]]
           [:td (percent-sub ::subs/treated-ten-year-risk)]
           [:td (percent-sub ::subs/treated-thirty-year-risk)]]
          [:tr
           [:td [:strong (t "Number to Treat to Prevent One Event")]]
           [:td (num-sub ::subs/number-to-treat-ten-years)]
           [:td (num-sub ::subs/number-to-treat-thirty-years)]]
          [:tr
           [:td [:strong (t "Risk Reduction Factor")]]
           [:td (percent-sub ::subs/ten-year-risk-reduction-percentage)]
           [:td (percent-sub ::subs/thirty-year-risk-reduction-percentage)]]]]]]
      [:div (t "Fill in the form to see your results.")])]])

(defn language-switch []
  (if-let [lang (translation/current)]
    (let [[text switch-to] (translation/switcher lang)]
      [:a {:on-click #(re-frame/dispatch [::ev/lang switch-to])}
       text])
    (let [[text switch-to] (translation/switcher config/startup-lang)]
      [:a {:href (if (= switch-to :en)
                   "../index.html"
                   "fr/index.html")}
       text])))

(defn copyright []
  [:div
   [:p (str '\u00A9
            " " "2018 George"'\u00A0 "Thanassoulis" ","
            " " "Allan" '\u00A0 "D." '\u00A0 "Sniderman" ","
            " & " "Michael" '\u00A0 "J." '\u00A0 "Pencina"
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
