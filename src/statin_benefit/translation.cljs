(ns statin-benefit.translation
  "This is a convenient, but ultimately sloppy approach to translation. Annotate
  every translatable string by wrapping it in the t function, and lookup the
  correct translation of that string at runtime.

  The danger is that chauvanism creeps in easily when people think that some
  things don't need translating..."
  (:require [re-frame.core :as re-frame]
            [statin-benefit.config :as config]
            [statin-benefit.subs :as subs]))

(defn current []
  @(re-frame/subscribe [::subs/lang]))

(def phrasebook
  { "MUHC-Duke Statin Benefit Calculator"
   "Calculateur de l'Avantage de Statines MUHC-Duke"

   "Are you currently being treated for hypertension?"
   "Êtes-vous actuellement en traitement pour l'hypertension?"

   "Fill in the form to see your results."
   "Complétez le formulaire pour voir vos résultats."

   "Number to Treat to Prevent One Event"
   "Numéro à Traiter pour Éviter un Événement"

   "Are you currently taking statins?"
   "Êtes-vous actuellement traité avec des statines?"

   "Do you currently smoke?"    "Fumez-vous actuellement?"
   "Are you diabetic?"          "Êtes-vous diab‌étique?"
   "Statin Treatment Intensity" "Dosage de Statines"
   "Source Code"                "Code Source"
   "References"                 "Références"
   "Age"                        "Âge"
   "Ethnicity"                  "Ethnie"
   "White"                      "Blanc / Blanche"
   "African American"           "Noir / Noire"
   "Sex"                        "Sexe"
   "Male"                       "Mâle"
   "Female"                     "Femelle"
   "Blood Pressure"             "Tension Artérielle"
   "Yes"                        "Oui"
   "No"                         "Non"
   "Cholesterol"                "Cholestérol"
   "Total"                      "Total"
   "Units"                      "Unités"
   "Select"                     "Choisir"
   "Low"                        "Faible"
   "Moderate"                   "Moyen"
   "High"                       "Haute"
   "Results"                    "Résultats"
   "Plus Ezetimibe"             "Avec Ezetimibe"
   "10 Year ASCVD Risk"         "Risque sur 10 Ans"
   "30 Year ASCVD Risk"         "Risque sur 30 Ans"
   "With Treatment"             "Avec Traitement"
   "Without Treatment"          "Sans Traitement"
   "Risk Reduction Factor"      "Réduction du Risque Relatif"
   "The authors"                "Les auteurs"
   "Released under the"         "Distribué sous le"})

(defn t [text]
  (let [lang (or (current) config/startup-lang)]
    (if (= lang :en)
      text
      (if-let [p (get phrasebook text)]
        p
        [:div.alarm "!@#$!@#$%"]))))

(defn switcher
  "Returns a pair of text and language key for the language currently not in
  use.
  N.B.: This assumes only English and French are options."
  [lang]
  (if (= :en lang)
    ["version française" :fr]
    ["english version" :en]))
