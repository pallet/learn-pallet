(use 'learn-pallet)
(defsection deploy-app
  :dependencies [[com.palletops/app-deploy-crate "0.8.0-alpha.3"]
                 [com.palletops/java-crate "0.8.0-beta.5"]
                 [com.palletops/runit-crate "0.8.0-alpha.1"]])

(ns deploy-app
  (:require
   [pallet.api :refer [converge group-spec server-spec node-spec plan-fn]]
   [pallet.crate.automated-admin-user :refer [automated-admin-user]]
   [pallet.crate.app-deploy :as app-deploy]
   [pallet.crate.java :as java]
   [pallet.crate.runit :as runit]))


;; (def
;;   ^{:doc "Defines the type of node deployer will run on"}
;;   base-server (server-spec :extends [learn-pallet/*base-spec*]))

(def
  ^{:doc "Define a server spec for deployer"}
  deployer-server
  (server-spec
   :extends
   [(java/server-spec {})
    (runit/server-spec {})
    (app-deploy/server-spec
     {:app-root "/opt/jenkins"
      :artifacts
      {:from-maven-repo
       [{:coord '[org.jenkins-ci.main/jenkins-war "1.529" :extension "war"]
         :path "jenkins.war"}]}
      :repositories {"jenkins"
                     {:url "http://repo.jenkins-ci.org/public"}}
      :run-command "java -jar /opt/jenkins/jenkins.war"})
    (runit/server-spec {})]))

(def
  ^{:doc "Defines a group spec that can be passed to converge or lift."}
  deployer
  (group-spec
   "deployer"
   :node-spec (assoc-in learn-pallet/*base-spec*
                       [:network :incoming-ports] 8080)
   :extends [deployer-server]))

(defn run []
  (converge {deployer 1}
            :compute learn-pallet/*compute*
            :phase [:install :configure :deploy]))

(defn destroy []
  (converge {deployer 0} :compute learn-pallet/*compute*))
