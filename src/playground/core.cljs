(ns ^:figwheel-hooks playground.core
  (:require [reagent.core :as r]))

(defn root-ui []
  [:div
   [:div "Welcome"]
   [:input]])

;; Reagent+Figwheel boilerplate

(defn reload []
  (r/render [root-ui] (js/document.getElementById "app")))

(defn ^:after-load on-reload []
  (reload))

(defn ^:export main
  []
  (reload))
