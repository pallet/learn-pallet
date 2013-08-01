(learn-pallet/bootstrap-ns pallet-users nil)
(ns pallet-users
  (:require [pallet.action :refer [with-action-options]]
            [pallet.actions :refer [remote-file user]]
            [pallet.crate :refer [defplan]]))

(def test-user "abcd")

(defplan  write-hello [n] (remote-file n  :content "hello world!"))

(def user-group
  (group-spec
   "user-group"
   :extends *base-spec*
   :phases {:configure (plan-fn (user test-user :create-home true))
            :default
            ;; by default, pallet logs in with your user, and then
            ;; sudoes to 'root',
            (plan-fn (write-hello "default.txt"))
            :sudo-as-user
            (plan-fn
             (with-action-options
               {:sudo-user test-user}
               (write-hello "sudo-as-user.txt")))}))

(defn run []
  (converge {user-group 1}
            :compute *compute*
            :phase [:configure :default :sudo-as-user]))

(defn destroy []
  (converge {user-group 0} :compute *compute*))
