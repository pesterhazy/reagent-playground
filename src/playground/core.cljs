(ns ^:figwheel-hooks playground.core
  (:require [reagent.core :as r]
            [clojure.pprint :refer [pprint]]
            [com.stuartsierra.mapgraph :as mg]))

(def the-id-attrs #{:slide/id :document/id})

(def empty-ds (apply mg/add-id-attr (mg/new-db) the-id-attrs))

(defn apply-op [ds [action payload lamport-ts :as op]]
  (assert (= :db/add action))
  (let [id-attr (->> payload keys (filter the-id-attrs) first)]
    (assert (some? id-attr))
    (let [lookup-ref [id-attr (get payload id-attr)]
          entity (get ds lookup-ref)
          existing-ts (:db/version entity)]
      (if (and existing-ts (not= -1 (compare existing-ts lamport-ts)))
        (do
          (println "Skipping outdated:" op)
          ds)
        (mg/add ds (assoc payload :db/version lamport-ts))))))

(def the-client-id 12345)
(def the-lamport-ts [1 the-client-id])

(def the-root [:document/id 1])

(def the-ops
  [[:db/add {:document/id 1} [0 the-client-id]]
   [:db/add {:slide/id 1, :slide/title "One"} [0 the-client-id]]
   [:db/add {:slide/id 2, :slide/title "Two"} [0 the-client-id]]
   [:db/add {:slide/id 1, :slide/title "Eins"} [0 the-client-id]]
   [:db/add {:document/id 1 :document/slides [{:slide/id 1} {:slide/id 2}]} [1 the-client-id]]])

(def the-ds (reduce apply-op empty-ds the-ops))

(defn root-ui []
  (pprint the-ds)
  [:pre (-> (mg/pull the-ds [{:document/slides '[*]}] the-root)
            pprint
            with-out-str)])

;; Reagent+Figwheel boilerplate

(defn reload []
  (r/render [root-ui] (js/document.getElementById "app")))

(defn ^:after-load on-reload []
  (reload))

(defn ^:export main
  []
  (reload))
