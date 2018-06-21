(ns ^:figwheel-hooks playground.core
  (:require [reagent.core :as r]
            [clojure.pprint :refer [pprint]]
            [datascript.core :as d]))

(def empty-ds {})

(defn apply-op [ds [action payload]]
  (case action
    :db/add
    (assoc ds 1234 payload)))

(def the-ops
  [[:db/add {:slide/id 1
             :slide/title "One"}]
   [:db/add {:slide/id 2
             :slide/title "Two"}]])


(defn root-ui []
  (prn (reduce apply-op empty-ds the-ops))
  [:div "hi"])
;; Reagent+Figwheel boilerplate

(defn reload []
  (r/render [root-ui] (js/document.getElementById "app")))

(defn ^:after-load on-reload []
  (reload))

(defn ^:export main
  []
  (reload))
