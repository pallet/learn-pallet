(learn-pallet/bootstrap-ns session-results nil)
(ns session-results
  (:require [pallet.actions :as actions]
            [pallet.api :as api]))

(def good-group
  (api/group-spec
   "good"
   :extends *base-spec*
   :phases
   {:first (api/plan-fn (actions/exec-script
                         (println "first!")))
    :second (api/plan-fn
                (actions/exec-checked-script
                 "say hello!"
                 ("echo" "hello world!")))}))

(def bad-group
  (api/group-spec
   "bad"
   :extends *base-spec*
   :phases
   {:first (api/plan-fn (actions/exec-script (println "first!")))
    :second (api/plan-fn
                (actions/exec-script
                 (println "hello world!"))
                (actions/exec-checked-script
                 "fail!"
                 ("exit -1")))}))

(defn run
  ([] (run 2 2))
  ([n] (run n 2))
  ([n m]
     (api/converge {good-group n
                bad-group m}
                   :compute *compute*
                   :phase [:first :second])))

(defn destroy
  (run 0 0))
