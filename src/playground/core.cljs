(ns ^:figwheel-hooks playground.core
  (:require [reagent.core :as r]
            [clojure.pprint :refer [pprint]]
            [datascript.core :as d]))

(def colors ["red" "blue" "brown"])

(defonce !counter (r/atom 0))
(defonce !view (r/atom {}))
(defonce !txs (r/atom []))

(defn transmogrify [xs]
  (mapv (partial vec) xs))

(defn update-view-ea [prev txs]
  (reduce (fn [view [e a v _ added?]]
            (update view e (fn [va]
                             (if added?
                               (assoc va a v)
                               (if (= v (get va a))
                                 (dissoc va a)
                                 va)))))
          prev
          txs))

(defn update-view-ae [prev txs]
  (reduce (fn [view [e a v _ added?]]
            (update view a (fn [va]
                             (if added?
                               (assoc va e v)
                               (if (= v (get va e))
                                 (dissoc va e)
                                 va)))))
          prev
          txs))

(defn on-update [v]
  (swap! !view (fn [view]
                 (-> view
                     (update :ea (fn [prev] (update-view-ea prev (:tx-data v))))
                     (update :ae (fn [prev] (update-view-ae prev (:tx-data v)))))))
  (swap! !txs into (:tx-data v)))

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
  [:pre
   (->> @!txs
        transmogrify
        pprint
        with-out-str)])

(defn view-viewer-ui []
  [:pre (-> @!view pprint with-out-str)])

(defn remove-item [e]
  (d/transact! !conn
               (->> (d/datoms @!conn :eavt e)
                    (map (fn [[e a v]] [:db/retract e a v])))))

(defn cycle-color [e]
  (let [color (->> (d/datoms @!conn :eavt e :todo/color) first :v)]
    (d/transact! !conn
                 [[:db/add e :todo/color (-> colors to-array (.indexOf color) inc (mod 3) colors)]])))

(defn edit-ui [view]
  (into [:ul]
        (->> view
             :ae
             :todo/caption
             keys
             (map (fn [e]
                    (-> view
                        :ea
                        (get e)
                        (assoc :db/id e))))
             (map-indexed (fn [idx ent]
                            [:li {:key idx}
                             [:span {:style {:color (:todo/color ent)}}
                              (:todo/caption ent)]
                             [:span " (" (:db/id ent) ")" " "]
                             [:button {:on-click #(remove-item (:db/id ent))} "X"]
                             [:span " "]
                             [:button {:on-click #(cycle-color (:db/id ent))} "COL"]])))))

(defn root-ui []
  [:div.p-5.my-form.bg-white
   [:div
    [:h3 "Reagent playground"]
    [:p "Type into this input box"]

    [:h4 "edit"]
    [edit-ui @!view]
    [:button {:on-click add-item} "add item"]
    [:h4 "view"]
    [view-viewer-ui]
    [:h4 "tx log"]
    [db-viewer-ui]
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
