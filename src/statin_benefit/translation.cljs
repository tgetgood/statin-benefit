(ns statin-benefit.translation
  "This is a convenient, but ultimately sloppy approach to translation. Annotate
  every translatable string by wrapping it in the t function, and lookup the
  correct translation of that string at runtime.

  The danger is that chauvanism creeps in easily when people think that some
  things don't need translating..."
  (:require [re-frame.core :as re-frame]
            [statin-benefit.subs :as subs]))

(defn current []
  @(re-frame/subscribe [::subs/lang]))

(def phrasebook
  {"Personalised Statin Benefit Calculator" "Prestation des Statines Personnalisée"
   "Source Code" "Code Source"
   "References" "Matériel de Référence"
   })

(defn t [text]
  (let [lang (current)]
    (if (= lang :en)
      text
      (if-let [p (get phrasebook text)]
        p
        [:div.alarm "!@#$!@#$%"]))))

(defn switcher
  "Returns a pair of text and language key for the language currently not in
  use.
  N.B.: This assumes only English and French are options."
  []
  (if (= :en (current))
    ["version française" :fr]
    ["english version" :en]))
