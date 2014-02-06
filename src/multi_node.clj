(defsection multi-node)

(ns multi-node
  (:require
   [pallet.api :as api]
   [pallet.crate :as crate]
   [pallet.node :as node]
   [pallet.actions :as actions]))

(def group-a
  (api/group-spec
   "group-a"
   :extends *base-spec*
   :phases
   {:store-ip (api/plan-fn
               (crate/assoc-settings
                :my-ip {:ip-address (node/primary-ip (crate/target-node))}))}))

(def group-b
  (api/group-spec
   "group-b"
   :extends *base-spec*
   :phases
   {:write-ips (api/plan-fn
                (let [ips (vec (map #(:ip-address (crate/get-node-settings % :my-ip))
                                    (crate/nodes-in-group :group-a)))]
                  (actions/remote-file "group-a-ips.txt"
                               :content (apply str (map #(format "%s\n" %) ips)))))}))

(defn run
  ([] (run 2))
  ([n]
     (api/converge {group-a n group-b 1} :compute *compute*
               :phase [:store-ip :write-ips])))

(defn destroy []
  (api/converge {group-a 0 group-b 0} :compute *compute*))
