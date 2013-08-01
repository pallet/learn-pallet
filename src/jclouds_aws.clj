(defsection jclouds-aws
  :dependencies [[com.palletops/pallet-jclouds "1.5.3"]
                 [org.jclouds.provider/aws-ec2 "1.5.5"]
                 [org.jclouds.driver/jclouds-slf4j "1.5.5"]
                 [org.jclouds.driver/jclouds-sshj "1.5.5"]])

(ns jclouds-aws
  (:require [pallet.api :as api]
            [pallet.compute :as compute]))

;; create the basic compute service
(def id "<your aws identity>")
(def cred "<your aws credendial>")
(def my-compute (compute/instantiate-provider :aws-ec2 :identity id :credential cred))

(comment
  ;; To list the current nodes in the account
  (compute/nodes my-compute)

  ;; to list the images available (caution, this will list ~11K images!)
  (def images (compute/images my-compute))
  (count images)
)

;; AWS offers by default all of the available of images via it's API
;; (~11K at the time of writing this), and this can make communication
;; with AWS quite slow in some instances.
;;
;; In most of the cases it make sense to tell jclouds to only consider
;; certain kind of images. For this, we can use the
;; 'jclouds.ec2.ami-query' filter property of the AWS provider. This
;; filter can have many cluses separated by a semicolon, and the
;; clauses are of type 'key=value1' or 'key=value1,value2,...value,n",
;; and the suppored keys can be found in the following URL, in the
;; section "supported filters"

;; http://docs.aws.amazon.com/AWSEC2/latest/APIReference/ApiReference-query-DescribeImages.html

;; The most common case is to limit the images by certain owners. An
;; owner can be 'amazon', 'amz-marketplace', 'self' or any valid AWS
;; accout ID. You can filter by one or more owners, and in the latter
;; case, the value for 'owner-id' is a comma-separated list of owner
;; IDs.

;; The following creates a compute service that will only see images
;; that you own of have explicit permissions for in and amazon,  and
;; are also available. If you own images, you can use your own amazon
;; id to list only the images you own.
(def my-filtered-compute
  (compute/instantiate-provider
   :aws-ec2
   :identity id
   :credential cred
   :jclouds.ec2.ami-query "owner-id=self,amazon;state=available"))

(comment
  (def images (compute/images my-compute))
  (count images)
  ;; ==> 497
  )

;; Regions and availability Zones
;; ------------------------------

;; JClouds creates nodes in the us-east-1 region by default, but you
;; can override the region and availability zone for each node. To do
;; so you use :location-id in the node spec. The following will launch
;; a node in the N. California region, in the availability group A
;; (us-west-1a)


(def us-west-1a-node-spec
  (api/node-spec
   :image {:os-family :ubuntu}
   :location {:location-id "us-west-1a"}))

(comment
  ;; launch a node
  (def s (api/converge
          {(api/group-spec "west" :node-spec us-west-1a-node-spec) 1}
          :compute my-compute))
  (explain-session s)
  ;; destroy the node
  (def s (api/converge
          {(api/group-spec "west" :node-spec us-west-1a-node-spec) 0}
          :compute my-compute))
  (explain session s)
  )

;; if you use an image-id in the node spec, make sure the image exists
;; in the region. The image name should have the format
;; `us-west-1/<image-id>` in the example above.

;; Security Groups and VPC
;;------------------------
;; This is currently broken. see https://github.com/pallet/pallet/issues/308

;; You can place your nodes in security groups or on a vpc.

(def vpc-node-spec
  (api/node-spec
   :image {:os-family :ubuntu}
   :location {:subnet-id "subnet-8beXXXX"}))

(comment
  ;; create a node on the VPC
  (api/converge
   {(api/group-spec "subnet" :node-spec vpc-node-spec) 1}
   :compute my-compute :os-detect false)
  ;; note that we turn of on-target os-detection oxn the node because
  ;; Pallet can't currently connect to the remote nodes on VPCs

  ;; destroy the node
   (api/converge {(api/group-spec "subnet") 0} :compute my-compute )
  )




