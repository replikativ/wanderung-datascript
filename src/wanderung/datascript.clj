(ns wanderung.datascript
  (:require
   [wanderung.core :as w :refer [create-source]]
   [datascript.core :as ds])
  (:import
   [wanderung.core IConnection IExtract]))

(defn resolve-schema [db]
  (->> db
       :schema
       (mapv (fn [[ident schema-def]]
               (assoc schema-def :db/ident ident)))))

(defrecord DataScriptDB [state db]
  IConnection
  (connect [{:keys [state db]}]
    (swap! state assoc :db db))
  IExtract
  (extract-datoms [{:keys [state]}]
    (->> (ds/datoms (:db @state) :eavt)
         (mapv (fn [d] (vec (seq d))))
         (sort-by #(nth % 3))
         vec)))

(defmethod create-source :datascript [{:keys [db]}]
  (map->DataScriptDB {:state (atom nil)
                      :db db}))
