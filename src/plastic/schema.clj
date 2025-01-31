(ns plastic.schema
  (:require [jsonista.core :as json]
            [clojure.string :as s]
            [yang.lang :as y]))

(defonce default-mapper (json/object-mapper {:decode-key-fn keyword}))

(defn to-table [{:keys [table
                        schema
                        columns
                        relationship]}]
  (let [relation (when-let [{:keys [type foreign_key]} relationship]
                   {:type (keyword type)
                    :parent (-> foreign_key :parent first)
                    :child (-> foreign_key :child first)})]
    (y/assoc-if {:name table
                 :fields columns} :schema schema
                                  :relation relation)))

(defn flatten-tables [{:keys [children] :as tables}]
  (let [current-table (to-table tables)
        child-tables (mapcat flatten-tables
                             children)]
    (cons current-table child-tables)))

(defn to-tables [jschema]
  (let [schema (json/read-value jschema
                                default-mapper)]
    {:index  (-> schema first :index (s/replace "%s" ""))
     :tables (-> schema first :nodes flatten-tables vec)}))
