(use 'learn-pallet)
(bootstrap-ns docker-riemann
              '[[com.palletops/pallet-docker "0.1.0"]
                [com.palletops/docker-crate "0.8.0-alpha.1"]
                [com.palletops/java-crate "0.8.0-beta.5"]
                [com.palletops/riemann-crate "0.8.0-alpha.2"]])

;; (defsection docker-riemann
;;   :dependencies [[com.palletops/java-crate "0.8.0-beta.4"]])

(ns docker-riemann
  "A learn-pallet lesson to create a Riemann container in Docker."
  (:require
   [clojure.pprint :refer [pprint]]
   [pallet.api :refer [converge group-spec node-spec plan-fn]]
   [pallet.compute.docker :refer [create-image]]
   [pallet.core.api :refer [phase-errors]]
   [pallet.core.plan-state :as plan-state]
   [pallet.crate.automated-admin-user :as automated-admin-user]
   [pallet.crate.docker :as docker]
   [pallet.crate.java :as java]
   [pallet.crate.riemann :as riemann]
   [pallet.node :as node]))

;;; # Docker Host Node

;;; The `docker-group` group-spec makes it easy to install docker, in this case
;;; on Ubuntu 13.04.
(def docker-group
  (group-spec :docker
    :extends [automated-admin-user/with-automated-admin-user
              (docker/server-spec {})]
    :phases {:bootstrap (plan-fn (automated-admin-user/automated-admin-user))}
    :node-spec (node-spec :image {:os-family :ubuntu
                                  :os-version-matches "13.04"})))

;;; A convenience function to start a docker host node
(defn start-docker-host
  []
  (let [res (converge {docker-group 1}
                      :compute *compute*
                      :phase [:install :configure])]
    (if-let [e (phase-errors res)]
      (pprint e)
      (:node (first (:targets res))))))

(defn stop-docker-host
  []
  (let [res (converge {docker-group 0}
                      :compute *compute*)]
    (when-let [e (phase-errors res)]
      (pprint e))))


;;; # Riemann Container
(def riemann-image-group
  (group-spec :riemann-image
    :extends [(java/server-spec {:version [6]})
              (riemann/server-spec {})]
    :node-spec (node-spec :image {:image-id "pallet/ubuntu2"
                                  :bootstrapped true})))

(defn riemann-container
  "Function to return the image-id of a docker image that has Riemann
  installed."
  [docker-service]
  (let [res (converge {riemann-image-group 1}
                      :compute docker-service
                      :phase [:install :configure]
                      :user {:username "root" :password "pallet"
                             :sudo-password "pallet"})]
    (when-let [e (phase-errors res)]
      (pprint e)
      (pprint res)
      (throw (ex-info "Problem starting container"
                      {:phase-errors e})))
    (clojure.tools.logging/infof "riemann-container res %s" res)
    (println "riemann-container res %s" res)
    (let [node (:node (first (:targets res)))
          _ (assert node "No node reported from container")
          settings (plan-state/get-settings
                    (:plan-state res) (node/id node) :riemann nil)
          id (create-image docker-service node {:tag "pallet/riemann"})]
      (converge {riemann-image-group 0} :compute docker-service)
      [id 5555])))

(defn riemann-group
  "Return a group-spec to run riemann in a container based on the specified
  image-id."
  [image-id port]
  (group-spec :riemann
    :node-spec {:image {:image-id image-id
                        :init "/opt/riemann/bin/riemann"}
                :network {:inbound-ports [22 port]}}))

(defn run-riemann
  "Run riemann in a docker container."
  [docker-service image-id port]
  (let [res (converge {(riemann-group image-id port) 1}
                      :compute docker-service
                      :phase [:install :configure]
                      :os-detect false)]
    (when-let [e (phase-errors res)]
      (pprint e)
      (throw (ex-info "Problem starting riemann"
                      {:phase-errors e})))
    ;; TODO - return the riemann port
    (:node (first (:targets res)))))
