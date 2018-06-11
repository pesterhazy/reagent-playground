(ns ^:figwheel-hooks playground.core
  (:require [reagent.core :as r]
            [clojure.pprint :refer [pprint]]
            [datascript.core :as d]))

(defonce !conn (d/create-conn))
(defonce !view (r/atom {}))

(defn input-ui []
  [:input.form-control {:type "text"
                        :id "test"
                        :placeholder "Your name..."}])

(defn add-item []
  (d/transact! !conn [{:db/id -1
                       :todo/caption "hello"}]))

(defn transmogrify [xs]
  (map (partial vec) xs))

(defn update-view [prev txs]
  (reduce (fn [view [e a v]]
            (update view e (fn [va]
                             (assoc va a v))))
          prev
          txs))

(defn db-viewer-ui []
  (r/with-let [!counter (r/atom 0)
               _ (d/listen! !conn :db-viewer (fn [v]
                                               (swap! !view (fn [prev] (update-view prev (:tx-data v))))
                                               (swap! !counter inc)))]
    @!counter
    [:pre (-> @!conn (d/datoms :eavt) transmogrify vec pprint with-out-str)]
    (finally
      (prn [:unlisten])
      (d/unlisten! !conn :db-viewer))))

(defn view-viewer-ui []
  [:pre (-> @!view pprint with-out-str)])


(defn root-ui []
  [:div.p-5.my-form.bg-white
   [:div
    [:h3 "Reagent playground"]
    [:p "Type into this input box"]
    [:h4 "view"]
    [view-viewer-ui]
    [:h4 "op log"]
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
