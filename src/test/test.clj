(learn-pallet/bootstrap-ns test.test
 '[[com.palletops/java-crate "0.8.0-beta.4"]])
(ns test.test
  (:require [pallet.crate.java :as java]))

(def my-group (group-spec "my-test"
                          :extends [base-spec
                                    (java/server-spec {})]))

(defn run []
  (converge {my-group 1} :compute *compute* :phase [:install :configure]))

(defn destroy []
  (converge {my-group 0} :compute *compute*))



