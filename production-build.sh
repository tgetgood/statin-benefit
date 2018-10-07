#!/usr/bin/env bash

cat resources/public/css/normalize.css resources/public/css/skeleton.css resources/public/css/app.css > build/main.css

clojure -m cljs.main --optimizations advanced --output-to "build/main.js" -c statin-benefit.core
