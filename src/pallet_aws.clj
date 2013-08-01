(defsection pallet-aws
  :dependencies [[com.palletops/pallet-aws "0.1.0"]
                 [org.slf4j/jcl-over-slf4j "1.7.4"]])

(ns pallet-aws
  (:require [pallet.api :as api]
            [pallet.compute :as compute]))

(def id "<your aws identity>")
(def cred "<your aws credendial>")

;(def service (compute/instantiate-provider))


;; create the basic compute service
(def my-compute (compute/instantiate-provider :pallet-ec2 :identity id :credential cred))

(comment
  ;; To list the current nodes in the account
  (compute/nodes my-compute)

  ;; to list the images available (caution, this will list 9K+ images!)
  (cmopute/images my-compute))


;; AWS offers by default all of the available of images via it's API
;; (9K+ atthe time of writing this), and this can make communication
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
;; from yourself and amazon, and images that are available
(def my-filtered-compute
  (compute/instantiate-provider
   :pallet-ec2
   :identity id
   :credential cred
   :jclouds.ec2.ami-query "owner-id=self,amazon;state=available"))

;; Regions and availability Zones
;; ------------------------------

;; JClouds creates nodes in the us-east-1 region by default, but you
;; can override the region and availability zone for each node. To do
;; so you use :location-id in the node spec. The following will launch
;; a node in the N. California region, in the availability group A
;; (us-west-1a)


(def us-west-1a-node-spec
  (api/node-spec
   :image {:image-id "ami-05355a6c"}
   :location { :location-id "us-west-1a"}))

(comment
  (api/converge
   {(api/group-spec "west" :node-spec us-west-1a-node-spec) 1}
   :compute my-compute))

;; if you use an image-id in the node spec, make sure the image exists
;; in the region. The image name should have the format
;; `us-west-1/<image-id>` in the example above.

;; Security Groups and VPC
;;------------------------

;; You can place your nodes in security groups or on a vpc.

(def vpc-node-spec
  (api/node-spec
   :image {:os-family :ubuntu}
   :location {:subnet-id "subnet-01904d6f"}))

(comment
  (api/converge
   {(api/group-spec "subnet" :node-spec vpc-node-spec) 1}
   :compute my-compute))
