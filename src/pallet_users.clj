(defsection pallet-users)
(ns pallet-users
  (:require [pallet.action :as action]
            [pallet.actions :as actions]
            [pallet.crate :as crate]
            [pallet.api :as api]))

(def test-user "tiffany")

(crate/defplan write-hello [n] (actions/remote-file n :content "hello world!"))

(def user-group
  (api/group-spec
   "user-group"
   :extends *base-spec*
   :phases {:configure (api/plan-fn
                        (actions/user test-user :create-home true))
            :default
            ;; by default, pallet logs in with your user, and then
            ;; sudoes to 'root',
            (api/plan-fn (write-hello "/tmp/default.txt"))
            :sudo-as-user
            (api/plan-fn
             (action/with-action-options
               {:sudo-user test-user}
               (write-hello "/tmp/sudo-as-user.txt")))}))

(defn run []
  (api/converge {user-group 1}
            :compute *compute*
            :phase [:configure :default :sudo-as-user]))

(defn destroy []
  (api/converge {user-group 0} :compute *compute*))
