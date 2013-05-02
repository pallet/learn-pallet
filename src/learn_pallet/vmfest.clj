(ns learn-pallet.vmfest
  "Namespace to guess the vbox.home system property, lifted from pallet-vmfest"
  (:use vmfest.manager
        [pallet.compute.vmfest.properties]
        [pallet.compute.vmfest :only [find-images install-image]]
        [clojure.pprint :only [print-table]]
        [clojure.java.io :only [file]]
        [clojure.tools.logging :only [debugf warnf]]))

(def ^:dynamic *image-url*
  "https://s3.amazonaws.com/vmfest-images/ubuntu-12.04.vdi.gz")

(defn boostrap-vmfest [compute]
  ;; get the vmfest connection from the provider
  (let [ubuntu-models (find-images compute {:os-family :ubuntu :os-version "12.04"})]
    (if (seq ubuntu-models)
      (do (println "*** Congratulations! Your setup already contains an Ubuntu 12.04 VM,")
          (println "***   we're ready to roll!"))
      ;; install the ubuntu image
      (do
        (println "*** To run these exercises we need do download a VM image.")
        (println "*** We're downloading an Ubuntu 12.04 VM image, it's about 300MB and")
        (println "***   it will take some time to download...")
        (try (let [return (install-image compute *image-url* {})]
               (println "*** VM image successfully downloaded and installed,")
               (println "***   we're ready to roll!"))
             (catch Exception e
               (println "*** Oops. Something went wrong trying to install the VM image.")))))))
