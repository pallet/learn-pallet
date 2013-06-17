(use 'learn-pallet)
(defsection admin-user)

(ns admin-user
  (:require
   [pallet.api :refer [converge group-spec lift]]
   [pallet.crate.automated-admin-user :refer [automated-admin-user
                                              with-automated-admin-user]]
   [pallet.compute :refer [nodes]]
   [pallet.node :refer [image-user]]))

;;; # Credentials and the Admin User

;;; # The Problem

;;; Pallet needs to be able to SSH into nodes to do it's work.  Pallet defines a
;;; concept of an admin user, and provides the `automated-admin-user` crate to
;;; help defining this user on the nodes you start.  This lesson explains the
;;; credentials defined by each image, why these shouldn't be used, and how to
;;; define your own user on the nodes that are started.

;;; ## Image Credentials

;;; On a cloud, or with vmfest, each image that you use to start a compute node
;;; has well known credentials.

;;; We can use the `start-node` function to start a node using `no-phase-group`,
;;; a `group-spec` with no phases defined, `(start-node no-phase-group)`.

(def no-phase-group (group-spec :no-phases
                      :extends [(dissoc *base-spec* :phases)]))

(defn start-node
  [group-spec]
  (converge {group-spec 1} :compute *compute*))

;;; To have pallet run a phase on this node, we would need to specify the user
;;; that pallet should SSH in with.

;;; One way to do this would be to pass a user map as the value of a `:user`
;;; keyword to `converge` or `lift`.

;;; The image credentials are reported by the `pallet.node/image-user` function
;;; on a node.  Run the `first-node-user` function to see the user reported for
;;; the node that was just started.

(defn first-node-user
  []
  (image-user (first (nodes *compute*))))

;;; We could use the reported image user as the the `:user` for a lift.  The
;;; `lift-ls` function does this, running a simple ls on the node.

(defn lift
  []
  (lift no-phase-group :compute *compute* :user (first-node-user)))

;;; This approach quickly gets tedious, and has a serious security issue
;;; stemming from the public nature of images and that the credentials are on
;;; each image are statically defined and well known (or easily discoverable).

;;; The `stop-node` function will shut down the node we just created,
;;; `(stop-node no-phase-group)`
(defn stop-node
  [group-spec]
  (converge {group-sepc 0} :compute *compute*))

;;; # The Solution

;;; ## Admin User

;;; In order to put you back in control, pallet defines a default user, the
;;; admin user, to be used to log into nodes.  The default user is held by the
;;; `pallet.core.user/*admin-user*` var.  It defaults to your local username,
;;; and your `id_rsa` ssh key.

;;; As we've seen above, new nodes know nothing of this admin user though, so
;;; pallet provides a mechanism to add the user to your new nodes.  This
;;; involves the `:bootstrap` phase, and the `automated-admin-user` crate.

;;; ## Bootstrap Phase

;;; The bootstrap phase is a phase that `converge` runs on new nodes, using the
;;; node's image credentials.  This makes it ideal for setting up the users you
;;; will need on the nodes.

;;; ## Automated Admin User

;;; The `automated-admin-user` crate provides an `automated-admin-user` which,
;;; by default, creates a user on the node, with credentials to match the admin
;;; user in use by pallet.  The `with-automated-admin-user` server-spec that can
;;; be used to add the `automated-admin-user` function to the `:bootstrap`
;;; phase via an `:extends` clause in your group specs.

;;; Putting this all together, we can now create a group-spec that authorises
;;; the admin user whenever a node is started.  The following two functions are
;;; equivalent.
(def base-group
  (group-spec :base-group
    :phases {:bootstrap (plan-fn (automated-admin-user))}))

(def base-group
  (group-spec :base-group
    :extends [with-automated-admin-user]))

;;; Now we can define start and stop nodes as before, this time using the
;;; `base-group` spec, `(start-node base-group)`.
