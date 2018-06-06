(ns ^:figwheel-hooks playground.core
  (:require [reagent.core :as r]))

(defn input-ui []
  [:input.form-control {:type "text"
                        :id "test"
                        :placeholder "Your name..."}])

(defn root-ui []
  [:div.p-5.my-form.bg-white
   [:div
    [:h3 "Reagent playground"]
    [:p "Type into this input box"]
    [:div.form-box
     [:form
      [:fieldset.form-group
       [:label {:for "test"} "Some input"]
       [input-ui]]]]]])

;; Reagent+Figwheel boilerplate

(defn reload []
  (r/render [root-ui] (js/document.getElementById "app")))

(defn ^:after-load on-reload []
  (reload))

(defn ^:export main
  []
  (reload))
