(ns wanderung.datascript-test
  (:require [clojure.test :refer :all]
            [datascript.core :as ds]
            [datahike.api :as dh]
            [wanderung.core :refer [migrate]]
            [wanderung.datascript :refer [resolve-schema]]))



(deftest test-simple-import
  (testing "simple data and no schema"
    (let [
          {datascript-db :db-after} (ds/with
                                     (ds/empty-db)
                                     [{:db/id 1
                                       :name  "Alice"
                                       :age   35}
                                      {:db/id 2
                                       :name  "Bob"
                                       :age   40}])
          source-cfg                {:db             datascript-db
                                     :wanderung/type :datascript}
          target-cfg                {:store           {:backend :mem
                                                       :id      "data-target"}
                                     :attribute-refs? false
                                     :keep-history?   false
                                     :wanderung/type  :datahike
                                     :name            "Data Target"}
          _                         (do
                                      (dh/delete-database target-cfg)
                                      (dh/create-database target-cfg))]
      (migrate source-cfg target-cfg)
      (let [dh-conn (dh/connect target-cfg)
            query '[:find ?n ?a
                    :where
                    [?e :name ?n]
                    [?e :age ?a]]]
        (is (= (ds/q query datascript-db)
               (dh/q query @dh-conn))))))
  (testing "simple data and schema"
    (let [datascript-schema         {:parents {:db/valueType   :db.type/ref
                                               :db/cardinality :db.cardinality/many}}
          {datascript-db :db-after} (ds/with
                                     (ds/empty-db datascript-schema)
                                     [{:db/id 1
                                       :name  "Alice"
                                       :age   35}
                                      {:db/id 2
                                       :name  "Bob"
                                       :age   40}
                                      {:db/id   3
                                       :name    "Charlie"
                                       :age     5
                                       :parents [1 2]}])
          source-cfg                {:db             datascript-db
                                     :wanderung/type :datascript}
          target-cfg                {:store           {:backend :mem
                                                       :id      "data-target"}
                                     :attribute-refs? false
                                     :keep-history?   false
                                     :wanderung/type  :datahike
                                     :initial-tx      (resolve-schema datascript-db)
                                     :name            "Data Target"}
          _                         (do
                                      (dh/delete-database target-cfg)
                                      (dh/create-database target-cfg))]
      (migrate source-cfg target-cfg)
      (let [dh-conn (dh/connect target-cfg)
            query '[:find ?n ?a
                    :where
                    [?e :name ?n]
                    [?e :age ?a]]]
        (is (= (ds/q query datascript-db)
               (dh/q query @dh-conn)))))))
