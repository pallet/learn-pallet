(use 'learn-pallet)
(defsection explain-plan)

(ns explain-plan
  (:require [pallet.repl :as repl]
            [pallet.actions :as actions]
            [pallet.api :as api]
            [pallet.crate.java :as java]))

;; Explain will show what actions and scripts result from the
;; execution of a phase funcion

(repl/explain-plan
 (api/plan-fn
  (actions/exec-script ("echo" "hello world!"))))
;; ==>
;; Mock lift with node: ["mock-node" "mock-group" "0.0.0.0" :ubuntu :os-version "12.04"]
;;   ACTION: pallet.actions/exec-script* of type script executed on target
;;     FORM:
;;       (pallet.actions/exec-script*
;;         "    # explain_plan.clj:7\necho hello world!")
;;     SCRIPT:
;;           # explain_plan.clj:7
;;       echo hello world!

;; you can turn of the printing of the forms or the printing of the scripts.
(repl/explain-plan
 (api/plan-fn (actions/exec-script ("echo" "hello world!")))
 :print-forms false)
;; ==>
;; Mock lift with node: ["mock-node" "mock-group" "0.0.0.0" :ubuntu :os-version "12.04"]
;;   ACTION: pallet.actions/exec-script* of type script executed on target
;;     SCRIPT:
;;           # NO_SOURCE_PATH:3
;;       echo hello world!

;; You can inspect how the same code will yield slightly different
;; scripts on different target Operating Systems

(def my-plan (api/plan-fn (actions/user "test-user" :groups ["group-a" "group-b"])))

;; By default, the plan is generated for Ubuntu 12.04
(repl/explain-plan my-plan :print-forms false)
;; ==>
;; Mock lift with node: ["mock-node" "mock-group" "0.0.0.0" :ubuntu :os-version "12.04"]
;;   ACTION: pallet.actions/user of type script executed on target
;;     SCRIPT:
;;           # user.clj:40
;;       if     # lib.clj:380
;;       getent passwd test-user; then
;;       # user.clj:42
;;           # lib.clj:413
;;       /usr/sbin/usermod --groups "group-a,group-b" test-user
;;       else
;;       # lib.clj:389
;;       /usr/sbin/useradd --groups "group-a,group-b" test-user
;;       fi

;; For CentOS 6, useradd/usermod take a different group switch (-G in
;; this case)
(repl/explain-plan my-plan :print-forms false :os-family :centos :os-version "6")
;; ==>
;; Mock lift with node: ["mock-node" "mock-group" "0.0.0.0" :centos :os-version "6"]
;;   ACTION: pallet.actions/user of type script executed on target
;;     SCRIPT:
;;           # user.clj:40
;;       if     # lib.clj:380
;;       getent passwd test-user; then
;;       # user.clj:42
;;           # lib.clj:423
;;       /usr/sbin/usermod -G "group-a,group-b" test-user
;;       else
;;       # lib.clj:403
;;       /usr/sbin/useradd -G "group-a,group-b" test-user
;;       fi

;; You can also explain a phase in a spec
(def my-group (api/group-spec "my-group"
                              :extends [( java/server-spec {})]))

(repl/explain-phase my-group :phase :install)

;; Mock lift with node: ["mock-node" "my-group" "0.0.0.0" :ubuntu :os-version "12.04"]
;;   ACTION: pallet.actions/package of type script executed on target
;;     FORM:
;;       (pallet.actions/package ("openjdk-7-jdk"))
;;     SCRIPT:
;;       echo '[install: install]: Packages...';
;;       {
;;       # package.clj:115
;;       apt-get -q -y install openjdk-7-jdk+ && \
;;       # package.clj:133
;;           # lib.clj:582
;;       dpkg --get-selections
;;        } || { echo '#> [install: install]: Packages : FAIL'; exit 1;} >&2
;;       echo '#> [install: install]: Packages : SUCCESS'
;;   IF nil THEN:
;;     ACTION: pallet.actions/exec-script* of type script executed on target
;;       FORM:
;;         (pallet.actions/exec-script*
;;           "echo 'Add java environment to /etc/environment...';\n{\n# environment.clj:34\npallet_set_env() {\nk=$1\nv=$2\ns=$3\n    # environment.clj:35\nif ! ( $(grep \"${s}\" /etc/environment) ); then\n# environment.clj:37\nsed -i -e \"/${k}/ d\" /etc/environment && \\\nsed -i -e \"$ a \\\\\n${s}\" /etc/environment || \\\nexit 1\nfi\n} && \\\n# environment.clj:31\nvv=$(dirname $(dirname $(update-alternatives --query javac | grep Best: | cut -f 2 -d ' ')))\n    # environment.clj:32\npallet_set_env JAVA_HOME ${vv} JAVA_HOME=\"${vv}\"\n } || { echo '#> Add java environment to /etc/environment : FAIL'; exit 1;} >&2 \necho '#> Add java environment to /etc/environment : SUCCESS'\n")
;;       SCRIPT:
;;         echo 'Add java environment to /etc/environment...';
;;         {
;;         # environment.clj:34
;;         pallet_set_env() {
;;         k=$1
;;         v=$2
;;         s=$3
;;             # environment.clj:35
;;         if ! ( $(grep "${s}" /etc/environment) ); then
;;         # environment.clj:37
;;         sed -i -e "/${k}/ d" /etc/environment && \
;;         sed -i -e "$ a \\
;;         ${s}" /etc/environment || \
;;         exit 1
;;         fi
;;         } && \
;;         # environment.clj:31
;;         vv=$(dirname $(dirname $(update-alternatives --query javac | grep Best: | cut -f 2 -d ' ')))
;;             # environment.clj:32
;;         pallet_set_env JAVA_HOME ${vv} JAVA_HOME="${vv}"
;;          } || { echo '#> Add java environment to /etc/environment : FAIL'; exit 1;} >&2 
;;         echo '#> Add java environment to /etc/environment : SUCCESS'
;;   IF nil THEN:
