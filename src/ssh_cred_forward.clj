(learn-pallet/bootstrap-ns ssh-cred-forward
                           '[[com.palletops/git-crate "0.8.0-alpha.1"]])
(ns ssh-cred-forward
  (:require [pallet.crate.git :as git]
            [pallet.actions :refer [remote-file user]]
            [pallet.action :refer [with-action-options]]))

;;(def private-repo "<your repo url here>")
(def private-repo "git@github.com:palletops/pallet-lxc.git"
  #_"https://github.com/palletops/pallet-lxc.git")

(def git-user "git-user")

(def git-user-home (str "/home/" git-user "/"))

(def ssh-config
  (str "Host github.com\n"
       "  HostName github.com\n"
       "  StrictHostKeyChecking no\n"))

(def git-group
  (group-spec
   "git-test"
   :extends [*base-spec* (git/git {})]
   :phases {:configure
            (plan-fn
             (user git-user)
             (with-action-options
               {:sudo-user git-user
                :script-dir git-user-home}
               (remote-file ".ssh/config" :content ssh-config)))
            :clone
            (plan-fn
             (with-action-options
               {:sudo-user git-user
                :script-dir git-user-home}
               (git/clone private-repo)))}))

(defn run []
  (converge {git-group 1}
            :compute *compute*
            :phase [:install :configure :clone]))

(defn destroy []
  (converge {git-group 0}
            :compute *compute*))
