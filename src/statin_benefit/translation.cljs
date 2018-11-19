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

   "warning: some parameters are outside the intended range of the model. the results might not be reliable."
   "attention: certains paramètres sont en dehors de la gamme prévue du modèle. les résultats pourraient ne pas être fiables."

   "Some data entered above is outside of the applicable range of the model."
   "certains paramètres sont en dehors de la gamme valide du modèle."

   "10 year risk calculation isn't applicable to people under 40."
   "Le calcul du risque de 10 ans n'est pas applicable au gens a moins de 40 ans."

   "warning: ASCVD risk increases under the proposed change."
   "attention: le risque de maladie cardiaque augmente."

   "Do you currently smoke?"   "Fumez-vous actuellement?"
   "Are you diabetic?"         "Êtes-vous diab‌étique?"
   "Statin Dosage"             "Dosage de Statines"
   "Current Statin Dosage"     "Dosage Actuel de Statines"
   "Prospective Statin Dosage" "Dosage Prospectif de Statines"
   "Source Code"               "Code Source"
   "References"                "Références"
   "Age"                       "Âge"
   "Ethnicity"                 "Ethnie"
   "White"                     "Blanc / Blanche"
   "African American"          "Noir / Noire"
   "Sex"                       "Sexe"
   "Male"                      "Mâle"
   "Female"                    "Femelle"
   "Blood Pressure"            "Tension Artérielle"
   "Yes"                       "Oui"
   "No"                        "Non"
   "Cholesterol"               "Cholestérol"
   "Total"                     "Total"
   "Units"                     "Unités"
   "Select"                    "Choisir"
   "None"                      "Zéro"
   "Low"                       "Faible"
   "Moderate"                  "Moyen"
   "High"                      "Haute"
   "Results"                   "Résultats"
   "Plus Ezetimibe"            "Avec Ezetimibe"
   "10 Year ASCVD Risk"        "Risque sur 10 Ans"
   "30 Year ASCVD Risk"        "Risque sur 30 Ans"
   "With Treatment"            "Avec Traitement"
   "Without Treatment"         "Sans Traitement"
   "Current Treatment"         "Traitement Actuel"
   "Prospective Treatment"     "Traitement Prospectif"
   "Risk Reduction Factor"     "Réduction du Risque Relatif"
   "The authors"               "Les auteurs"
   "Released under the"        "Distribué sous le"

   "Must be between"                "Doit être entre"
   "Must be greater than"           "Doit être supérieur à"
   "Must be less than"              "Doit être inférieur à"
   "Intended range is between"      "La gamme prévue est entre"
   "Intended range is greater than" "La gamme prévue est supérieure à"
   "Intended range is below"        "La gamme prévue est inférieure à"

   ;; This method is starting to breakdown.
   " "   " "
   "."   "."
   "and" "et"})

(defn t [text]
  (let [lang (or (current) config/startup-lang)]
    (if (= lang :en)
      text
      (if-let [p (get phrasebook text)]
        p
        [:div.alarm "!@#$!@#$%"]))))

(defn t* [ & bits]
  (apply str (map (fn [x] (if (string? x) (t x) x)) bits)))

(defn switcher
  "Returns a pair of text and language key for the language currently not in
  use.
  N.B.: This assumes only English and French are options."
  [lang]
  (if (= :en lang)
    ["version française" :fr]
    ["english version" :en]))
