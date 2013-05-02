(ns learn-pallet
  (:require [alembic.still :refer [distill]]
            [pallet.repl :refer :all]
            [pallet.compute :refer [instantiate-provider]]
            [pallet.crate.automated-admin-user :refer [with-automated-admin-user]]))

(def vmfest-deps
   "Dependencies to make vmfest work"
  '[[org.clojars.tbatchelli/vboxjxpcom "4.2.4"]
    [org.cloudhoist/pallet-vmfest "0.3.0-alpha.3"]])

(defn distill-all
  "Distills a sequence of lein-style libs"
  [deps]
  (doseq [dep deps] (distill dep)))

(def ^:dynamic
  *compute*
  "The compute provider to be used exercises"
  nil)

(def ^:dynamic
  *node-spec*
  "The node-spec to be used in all exercises"
  {:image {:image-id :ubuntu-12.04}})

(def ^:dynamic
  *base-spec*
  "The default group to be extended by group-specs in all exercises"
  (pallet.api/group-spec "learn-pallet-base-spec"
                         :node-spec *node-spec*
                         :extends [with-automated-admin-user]))

(defn bootstrap-ns*
   "boostraps a namespace to use pallet-repl"
  []
  (use-pallet))

(defn bootstrap
  "Boostraps the project, based on the `provider`. In the case of
  `:vmfest` it will downlaod and install an appropriate image if not
  found.

  Currently supports :vmfest only."
  [provider]
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

(defmacro bootstrap-ns
  "Bootstraps the namespace `ns` by downloading (if necessary) and
  installing in the classpath the listed dependencies (lein-style),
  e.g:

    (learn-pallet/bootstrap-ns test.test
      '[[com.palletops/java-crate \"0.8.0-beta.4\"]])

  Then it will bind `learn-clojure` and `pallet.repl` into the
  namespace."
  [ns deps]
  `(do
     (distill-all ~deps)
     (in-ns '~ns)
     (clojure.core/use '~'learn-pallet)
     (bootstrap-ns*)))

(defmacro switch-ns
  "Switches the repl to a new namespace `ns`. It does so by loading
  such namespace first and possibly rprompt the execution of the
  `boostrap-ns` macro to load dependencies into the classpath."
  [ns]
  `(do ;; make sure the namespace is boostrapped by requiring it
     (require '~ns :reload)
     ;; switch to the namespace
     (in-ns '~ns)
     ;; Make it look and feel like the 'user' namespace at the repl
     (apply require clojure.main/repl-requires)
     ;; bind learn-pallet into this namespace so that global symbols
     ;; are present
     (clojure.core/use '~'learn-pallet)))
