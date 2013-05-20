(ns learn-pallet.vmfest
  "Namespace to guess the vbox.home system property, lifted from pallet-vmfest"
  (:use vmfest.manager
        [pallet.compute.vmfest.properties]
        [pallet.compute.vmfest :only [has-image? install-image]]
        [clojure.pprint :only [print-table]]
        [clojure.java.io :only [file]]
        [clojure.tools.logging :only [debugf warnf]]))

(def ^:dynamic *image*
  {:vagrant? false
   :url "https://s3.amazonaws.com/vmfest-images/ubuntu-12.04.vdi.gz"
   :image-id :ubuntu-12.04})

#_(def ^:dynamic *image*
  {:vagrant? true
   :url "http://files.vagrantup.com/precise32.box"
   :os-family :ubuntu
   :os-version "12.04"
   :os-64-bit false
   :os-type-id "Ubuntu_32",
   :image-id :precise32})

(defn- set-base-node-spec! [image-id]
  (let [base-spec (ns-resolve 'learn-pallet 'base-spec)]
    (alter-var-root
     (ns-resolve 'learn-pallet '*base-spec*)
     (constantly
      (base-spec {:image {:image-id image-id}})))))

(defn bootstrap-vmfest [compute & {:keys [image-id] :as options}]
  ;; get the vmfest connection from the provider
  (let [supplied-image-id? (not (nil? image-id))
        image-id (or image-id (:image-id *image*))
        image-found? (has-image? compute image-id)]
    (if image-found?
      (do
        ;; reset the node spec to be used base on the selected image-id
        (set-base-node-spec! image-id)
        (println
         "*** Congratulations! Your setup already contains an image with id"
         image-id)
        (println "*** This means we're ready to roll :)"))

      (if supplied-image-id?
        ;; if we get here and the user supplied an image-id, this is an
        ;; error, otherwise, if no image-id is provided, install the
        ;; default one.
        (do
          ;; the id is wrong, don't try to install anything
          (println "*** There is no image with the id you supplied " image-id)
          (println "*** Available images are:"
                   (keys (pallet.compute/images compute))))
        (do
          ;; the default image is not installed. Install it
          (println "*** To run these exercises we need do download a VM image.")
          (println "*** We're downloading an proper image that is about 300MB and")
          (println "***   it will take some time to download...")
          (try (let [{:keys [vagrant? url]} *image*]
                 (if vagrant?
                   (let [meta (select-keys *image* [:os-type-id :os-family
                                                    :os-version :os-64-bit])]
                     (install-image compute url {:meta meta})
                     (set-base-node-spec! (:image-id *image*)))
                   (install-image compute url))
                 (println "*** VM image successfully downloaded and installed,")
                 (println "***   we're ready to roll!"))
               (catch Exception e
                 (println
                  "*** Oops. Something went wrong trying to install the VM image."
                  e))))))))

