(ns ^:figwheel-hooks playground.core
  (:require [reagent.core :as r]
            [clojure.pprint :refer [pprint]]
            [datascript.core :as d]))

(defonce !conn (d/create-conn))

(defn input-ui []
  [:input.form-control {:type "text"
                        :id "test"
                        :placeholder "Your name..."}])

(defn add-item []
  (d/transact! !conn [{:db/id -1
                       :todo/caption "hello"}]))

(defn transmogrify [xs]
  (map (partial vec) xs))

(defn db-viewer-ui []
  (let [!counter (r/atom 0)]
    (d/listen! !conn (fn []
                       (swap! !counter inc)))
    (fn []
      (prn @!counter)
      [:pre (-> @!conn (d/datoms :eavt) transmogrify vec pprint with-out-str)])))

(defn root-ui []
  [:div.p-5.my-form.bg-white
   [:div
    [:h3 "Reagent playground"]
    [:p "Type into this input box"]
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
