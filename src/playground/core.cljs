(ns playground.core
  (:require [reagent.core :as r]))

(defn root-ui []
  [:div "Welcome"])

(defn ^:export main
  []
  (r/render [root-ui] (js/document.getElementById "app")))
