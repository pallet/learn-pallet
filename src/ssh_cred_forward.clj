(defsession ssh-cred-forward
  :dependencies [[com.palletops/git-crate "0.8.0-alpha.1"]])

(ns ssh-cred-forward
  (:require [pallet.api :refer [converge group-spec plan-fn]]
            [pallet.crate :refer [admin-user]]
            [pallet.crate.git :as git]
            [pallet.actions :refer [directory remote-file user]]
            [pallet.action :refer [with-action-options]]))

;;(def private-repo "<your repo url here>")
(def private-repo "git@github.com:palletops/pallet-lxc.git"
  #_"https://github.com/palletops/pallet-lxc.git")

(def ssh-config
  (str "Host github.com\n"
       "  LogLevel DEBUG\n"
       "  IdentityFile ~/.ssh/id_dsa\n"
       "  StrictHostKeyChecking no\n"))

;;; Set this to your github username if it isn't the same as your
;;; admin-user name.
(defonce github-user (atom nil))

(def git-group
  (group-spec
      "git-test"
    :extends [*base-spec* (git/git {})]
    :phases {:configure
             (plan-fn
               (with-action-options {:script-prefix :no-sudo}
                 (directory ".ssh" :mode "755")
                 (remote-file
                  ".ssh/config"
                  :content (str ssh-config
                                "  User "
                                (or @github-user
                                    (:username (admin-user)))
                                "\n"))))
             :clone
             (plan-fn
               (with-action-options {:script-prefix :no-sudo
                                     :ssh-agent-forwarding true}
                 (git/clone private-repo)))}))

(defn run []
  (converge {git-group 1}
            :compute *compute*
            :phase [:install :configure :clone]))

(defn destroy []
  (converge {git-group 0}
            :compute *compute*))
