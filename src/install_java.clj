(defsection install-java
  :dependencies [[com.palletops/java-crate "0.8.0-beta.4"]])

(ns install-java
  (:require [pallet.crate.java :as java]
            [pallet.api :as api]))

(def my-group (api/group-spec "my-test"
                          :extends [*base-spec*
                                    (java/server-spec {})]))

(defn run []
  (api/converge {my-group 1} :compute *compute* :phase [:install :configure]))

(defn destroy []
  (api/converge {my-group 0} :compute *compute*))



