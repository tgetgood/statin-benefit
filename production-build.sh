#!/usr/bin/env bash

test -d resources/production || mkdir resources/production

cat resources/public/css/normalize.css resources/public/css/skeleton.css resources/public/css/app.css > resources/public/production/main.css

clojure -m cljs.main --optimizations advanced --output-to "resources/public/production/main.js" -c statin-benefit.core
