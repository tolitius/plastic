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

(defn to-tables
  "convert nested json schema to a one level edn map"
  [jschema]
  (let [schema (json/read-value jschema
                                default-mapper)]
    {:index  (-> schema first :index (s/replace "%s" ""))
     :tables (-> schema first :nodes flatten-tables vec)}))

(defn to-bash-map
  "convert an edn schema map to a bash map:

   root_table_name=\"quantum_tunnel\"
   declare -A schema_fields
   schema_fields[\"quantum_tunnel\"]='[\"id\", \"quantum_code\", \"quantity\", \"particle_number\", \"created_date\", \"last_modified_date\"]'
   schema_fields[\"quantum_code\"]='[\"category\", \"milestones\", \"high_point\"]'
   schema_fields[\"quantum_zone\"]='[\"id\", \"name\"]'
  "
  [{:keys [tables]}]
  (->> tables
       (group-by :name)
       (map (fn [[name tables]]
              [name (distinct (mapcat :fields tables))]))
       (map (fn [[name fields]]
              (str "schema_fields[\"" name "\"]='[\"" (s/join "\", \"" fields) "\"]'")))
       (into [(str "root_table_name=\"" (:name (first tables)) "\"")
              "declare -A schema_fields"])
       (s/join "\n")))
