(ns plastic.test.schema
  (:require [clojure.test :as t :refer [deftest is]]
            [plastic.schema :as ps]))

(deftest should-flatten-tables
  (let [tables (-> "test/resources/book.json"
                   slurp
                   ps/to-tables)]
    (is (= {:index "book"
            :tables
            [{:name "book"
              :schema "library"
              :fields ["id" "title"]}
             {:name "author"
              :schema "literature"
              :fields ["id" "name"]
              :relation {:type :one_to_one
                         :parent "parent_id"
                         :child "id"}}
             {:name "publisher"
              :schema "literature"
              :fields ["id" "name" "address"]
              :relation {:type :many_to_many
                         :parent "book_id"
                         :child "id"}}]}
           tables))))
