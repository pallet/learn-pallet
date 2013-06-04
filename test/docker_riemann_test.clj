(ns docker-riemann-test
  (:require
   [clojure.test :refer :all]
   [docker-riemann :refer :all]
   [pallet.compute :refer [destroy-node instantiate-provider nodes]]))

(deftest docker-riemann-test
  (let [host (start-docker-host)
        docker (instantiate-provider :docker :node host)]
    (nodes docker)
    (let [[image-id port] (riemann-container docker)
          riemann-node (run-riemann docker image-id port)]
      (destroy-node docker riemann-node))))
