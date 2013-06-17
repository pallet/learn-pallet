(ns learn-pallet
  (:require [alembic.still :refer [distill]]
            [pallet.repl :refer :all]
            [pallet.compute :refer [instantiate-provider]]
            [pallet.crate.automated-admin-user :refer [with-automated-admin-user]]
            [pallet.api :refer [compute-service]]))

(def provider-deps
  {:vmfest '[[com.palletops/pallet-vmfest "0.3.0-alpha.5"]]
   :ec2 '[[org.cloudhoist/pallet-jclouds "1.5.2"]
          [org.jclouds.provider/aws-ec2 "1.5.5"]
          [org.jclouds.provider/aws-s3 "1.5.5"]
          [org.jclouds.driver/jclouds-slf4j "1.5.5"]
          [org.jclouds.driver/jclouds-sshj "1.5.5"]]})


(defn distill-all
  "Distills a sequence of lein-style libs"
  [deps]
  (doseq [dep deps]
    (distill dep :verbose false)))

(defonce ^{:dynamic true
           :doc "The compute provider to be used exercises"}
  *compute* nil)

;; ubuntu precise 64-bit EBS for us-east-1 region
(def ec2-node-spec
  {:image {:image-id "us-east-1/ami-e50e888c"}
   :location { :location-id "us-east-1a"}})


(defn base-spec [node-spec]
  (pallet.api/group-spec "learn-pallet-base-spec"
                         :node-spec node-spec
                         :extends [with-automated-admin-user]))

(defonce ^:dynamic
  *base-spec*
  nil)

(defn- load-vmfest
  "Sets VMFest as the compute provider"
  [& {:keys [provider] :as opts}]
  ;; instantiate the provider first to load the right vbox lib into
  ;; the classpath
  (let [opts (dissoc opts :provider)
        flattened-opts (mapcat identity opts)
        compute (if provider
                  (apply compute-service provider opts)
                  (apply instantiate-provider :vmfest flattened-opts))]
    (require 'pallet.compute.vmfest 'learn-pallet.vmfest)
    (let [bootstrap-vmfest (ns-resolve 'learn-pallet.vmfest
                                       'bootstrap-vmfest)]
      (alter-var-root #'*compute* (constantly compute))
      (apply bootstrap-vmfest *compute* flattened-opts))))

(defn- load-ec2
  "Sets EC2 as the compute provider"
  [& {:keys [provider] :as opts}]
  (let [opts (dissoc opts :provider)
        flattened-opts (mapcat identity opts)
        compute (if provider
                  (apply compute-service provider opts)
                  (apply instantiate-provider :aws-ec2 flattened-opts))]
    (alter-var-root #'*compute* (constantly compute))
    (alter-var-root #'*base-spec* (constantly (base-spec ec2-node-spec)))
    (println
     "*** Congratulations! You're connected to Amazon Web Services. Enjoy!")))

(defn bootstrap
  "Boostraps the project, based on the `provider`. Providers allowed are:

  - :vmfest -> for VirtualBox via XPCOM (native)
  - :vmfest-ws -> for VirtualBox via Web Services. Requires `vboxwebsrv` running.
  - :ec2 -> for Amazon EC2. Requires :identity and :credential parameters."
  [provider & opts]
  ;; pallet caches the list of available providers on the first run,
  ;; and doesn't update it afterwards. We load new providers at
  ;; runtime, so we need to it to reload the list, and we force it by
  ;; resetting the provider list. Hack, I know....
  (reset! pallet.compute.implementation/provider-list nil)
  (condp = provider
    :vmfest (do (distill-all (:vmfest provider-deps))
                (apply load-vmfest opts))
    :vmfest-ws (do (distill-all (:vmfest provider-deps))
                   (apply load-vmfest :vbox-comm :ws (rest opts)))
    :ec2 (do (distill-all (:ec2 provider-deps))
             (apply load-ec2 opts))))

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
     (clojure.core/use '~'learn-pallet)))

(defmacro defsection
  "Defines a section namespace `ns` by downloading (if necessary) and
  installing in the classpath the listed dependencies (lein-style),
  e.g:

    (defsection test.test
      :dependencies [[com.palletops/java-crate \"0.8.0-beta.4\"]])

  Then it will use `learn-clojure` and `pallet.repl` into the
  namespace."
  [ns & {:keys [dependencies]}]
  `(do
     (distill-all '~dependencies)
     (in-ns '~ns)
     (clojure.core/use '~'learn-pallet)))

(defmacro section
  "Switches the repl to a new learn-pallet section. It does so by loading the
  section namespace, and using `learn-pallet` and `pallet.repl`."
  [ns]
  `(do ;; make sure the namespace is boostrapped by requiring it
     (require '~ns :reload)
     ;; switch to the namespace
     (in-ns '~ns)
     ;; Make it look and feel like the 'user' namespace at the repl
     (apply require clojure.main/repl-requires)
     ;; bind learn-pallet into this namespace so that global symbols
     ;; are present
     (clojure.core/use '~'learn-pallet)
     ;; bring also the basic pallet.repl functions
     (clojure.core/use '~'pallet.repl)))

(def ^:macro switch-ns @#'section)
;; (alter-var-root #'switch-ns with-meta (meta #'section))
