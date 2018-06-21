(ns ^:figwheel-hooks playground.core
  (:require [reagent.core :as r]
            [clojure.pprint :refer [pprint]]
            [com.stuartsierra.mapgraph :as mg]))

(def empty-ds (-> (mg/new-db)
                  (mg/add-id-attr :slide/id :document/id)))

(defn apply-op [ds [action payload]]
  (case action
    :db/add
    (mg/add ds payload)))

(def the-root [:document/id 1])

(def the-ops
  [[:db/add {:document/id 1}]
   [:db/add {:slide/id 1
             :slide/title "One"}]
   [:db/add {:slide/id 2
             :slide/title "Two"}]
   [:db/add {:document/id 1
             :document/slides [{:slide/id 1} {:slide/id 2}]}]])

(def the-ds (reduce apply-op empty-ds the-ops))

(defn root-ui []
  (pprint the-ds)
  [:pre (pr-str (mg/pull the-ds [{:document/slides '[*]}] the-root))])

;; Reagent+Figwheel boilerplate

(defn reload []
  (r/render [root-ui] (js/document.getElementById "app")))

(defn ^:after-load on-reload []
  (reload))

(defn ^:export main
  []
  (reload))
