# Learn Pallet

A project to help you learn pallet via live-coding.

## Usage

So far there is enough to bootstrap the live coding environment on
VMFest/VirtualBox, automatically downloading and installing an image. 

This only works for OSX now and some linux versions, just because it
uses VMFest with the XPCOM library. Can be fixed though, I just need
to get to it.

```clojure
;; this will get you a working VMFest if you have VirtualBox
;; already installed. If you don't have an Ubuntu 12.04 image
;; installed, it will download and install one, so be patient!
user> (use 'learn-pallet)(bootstrap :vmfest)

;; switch to a name-space. This will also import the jars needed from
;; clojars and/or sonatype and they'll be put in the classpath
;; automatically. It can take some time to do so.
user> (switch-ns install-java)

;; you're ready to run the exercise
test.test> (build)

;; Cool! Now let's remove the VMs created in this exercise.
test.test> (destroy)

;; Now it's time to switch to a new exercise...
test.test> (switch-ns ...)

```

Each individual exercise file starts with a call to
`learn-pallet/bootstrap-ns` listing the dependencies needed for this
exercise using the leiningen coordinates, e.g.:

```clojure
(learn-pallet/bootstrap-ns test.test
 '[[com.palletops/java-crate "0.8.0-beta.4"]])
```

When the file is loaded, this dependencies will be fetched from the
repos and loaded into the current classpath.

`learn-pallet` also provides the exercises with a default provider
`*compute*` and a default spec `base-spec` that defines the node-spec
for the ubuntu image, and also authorizes you to log into the node.

## License

Copyright © 2013 Antoni Batchelli

Distributed under the Eclipse Public License, the same as Clojure.
