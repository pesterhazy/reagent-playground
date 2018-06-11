(ns ^:figwheel-hooks playground.core
  (:require [reagent.core :as r]
            [clojure.pprint :refer [pprint]]
            [datascript.core :as d]))

(defonce !counter (r/atom 0))
(defonce !view (r/atom {}))

(defn transmogrify [xs]
  (map (partial vec) xs))

(defn update-view-ea [prev txs]
  (reduce (fn [view [e a v]]
            (update view e (fn [va] (assoc va a v))))
          prev
          txs))

(defn update-view-ae [prev txs]
  (reduce (fn [view [e a v]]
            (update view a (fn [va] (assoc va e v))))
          prev
          txs))

(defn on-update [v]
  (prn [:on-update v])
  (swap! !view (fn [view]
                 (-> view
                     (update :ea (fn [prev] (update-view-ea prev (:tx-data v))))
                     (update :ae (fn [prev] (update-view-ae prev (:tx-data v)))))))
  (swap! !counter inc))

(defn make-conn []
  (let [conn (d/create-conn)]
    (d/listen! conn :db-viewer on-update)
    (d/transact! conn [{:db/id -1
                        :todo/color "red"
                        :todo/caption "clean the fridge"}
                       {:db/id -2
                        :todo/color "blue"
                        :todo/caption "take out the garbage"}
                       {:db/id -3
                        :todo/color "brown"
                        :todo/caption "do the laundry"}])
    conn))

(defonce !conn (make-conn))

(defn input-ui []
  [:input.form-control {:type "text"
                        :id "test"
                        :placeholder "Your name..."}])

(defn add-item []
  (d/transact! !conn [{:db/id -1
                       :todo/color (rand-nth ["red" "blue" "brown"])
                       :todo/caption "hello"}]))

(defn db-viewer-ui []
  @!counter
  [:pre (-> @!conn (d/datoms :eavt) transmogrify vec pprint with-out-str)])

(defn view-viewer-ui []
  [:pre (-> @!view pprint with-out-str)])

(defn edit-ui [view]
  [:pre
   (->> view
        :ae
        :todo/caption
        keys
        (map (fn [e]
               (-> view :ea (get e))))
        (map-indexed (fn [idx ent]
                       [:div {:key idx}
                        [:span {:style {:color (:todo/color ent)}}
                         (:todo/caption ent)]]))
        )])

(defn root-ui []
  [:div.p-5.my-form.bg-white
   [:div
    [:h3 "Reagent playground"]
    [:p "Type into this input box"]

    [:h4 "edit"]
    [edit-ui @!view]
    [:h4 "view"]
    [view-viewer-ui]
    [:h4 "tx log"]
    [db-viewer-ui]
    [:button {:on-click add-item} "add item"]
    #_[:div.form-box
       [:form
        [:fieldset.form-group
         [:label {:for "test"} "Operation:"]
         [:input.form-control {:type "text"
                               :id "test"
                               :placeholder "op"}]]]]]])
;; Reagent+Figwheel boilerplate

(defn reload []
  (r/render [root-ui] (js/document.getElementById "app")))

(defn ^:after-load on-reload []
  (reload))

(defn ^:export main
  []
  (reload))
