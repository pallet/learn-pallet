(ns learn-pallet
  (:use [alembic.still :only [distill]]
        [pallet.repl]
        [pallet.compute :only [instantiate-provider]]
        [pallet.crate.automated-admin-user :only (with-automated-admin-user)]))


(def vmfest-deps '[[org.clojars.tbatchelli/vboxjxpcom "4.2.4"]
                   [org.cloudhoist/pallet-vmfest "0.3.0-alpha.3"]])

(defn distill-all [deps]
  (doseq [dep deps]
    (println "distilling" dep)
    (distill dep)))

(def ^:dynamic *compute* nil)

(def ^:dynamic *node-spec* {:image {:image-id :ubuntu-12.04}})

(def ^:dynamic base-spec (pallet.api/group-spec "learn-pallet-base-spec"
                                     :node-spec *node-spec*
                                     :extends [with-automated-admin-user]))

(defn bootstrap-ns* []
  (use-pallet))

(defn bootstrap [provider]
  (bootstrap-ns*)
  (condp = provider
    :vmfest (do
              (distill-all vmfest-deps)
              (require  'pallet.compute.vmfest 'learn-pallet.vmfest)
              (let [bootstrap-vmfest (ns-resolve 'learn-pallet.vmfest
                                                 'boostrap-vmfest)
                    compute (instantiate-provider :vmfest)]
                (alter-var-root #'*compute* (constantly compute))
                (bootstrap-vmfest *compute*)))))



(defmacro bootstrap-ns [ns deps]
  `(do
     (println ~deps)
     (when (seq ~deps)) (distill-all ~deps)
     (in-ns '~ns)
     (println *ns*)
     (clojure.core/use '~'learn-pallet)
     (bootstrap-ns*)))

(defmacro switch-ns [ns]
  `(do ;; make sure the namespace is boostrapped by requiring it
     (require '~ns :reload)
     ;; switch to the namespace
     (in-ns '~ns)
     (println "In NS:"*ns*)
     ;; make it just like 'user
     ;; same as the repl
;;     (clojure.core/refer 'clojure.core)
     (require '[clojure.repl :refer (~'source ~'apropos ~'dir ~'pst ~'doc ~'find-doc)]
              '[clojure.java.javadoc :refer (~'javadoc)]
              '[clojure.pprint :refer (~'pp ~'pprint)])
     (println "Required repl namespaces")
     (clojure.core/use '~'learn-pallet)
     (println "Required learn-pallet")
     ))
