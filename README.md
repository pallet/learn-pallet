# Learn Pallet

A project to help you learn pallet via live-coding.

## Running the Exercises

Before you can run the code in the exercises, you need to
[start a session](wiki/Starting-a-Session).

Each exercise in `learn-pallet` lives in its own namespace. We will
use `switch-ns` to switch exercises (namespaces). E.g. to run the code
for the `install-java` exercise we do:

```clojure
user=> (switch-ns install-java)
  ...
install-java=>
```

Running this command will download and install a few libraries, so it
might take some time to come back. Notice that now your REPL is now on
a different namespace than before: `install-java`.

This particular exercise has a `run` function that will create a new
VM and get java installed on it.

```clojure
install-java=> (def result (run))
```

The output of running this function will be stored in the variable
`result`. Feel free to inspect this variable, e.g. `(pprint result)`.
Also, Pallet logs all the actions it performs in the file
`logs/pallet.log`.

You can verify that Java is installed on the newly created node by
SSH-ing into it. First we need to get the IP Address of your node:

```clojure
install-java=> (show-nodes *compute*)

|        :hostname | :group-name | :os-family | :os-version |  :primary-ip |   :private-ip | :terminated? |
|------------------+-------------+------------+-------------+--------------+---------------+--------------|
| my-test-e3a1928c |     my-test |    :ubuntu |       12.04 | 107.22.55.13 | 10.80.187.174 |        false |
```

In this case, our node is `my-test-e3a1928c` (this depends on the
exercise), and the IP address that we want is the _primary_ one:
`107.22.55.13`. At the shell, run the following:

```shell
bash$ ssh 107.22.55.13

Welcome to Ubuntu 12.04.1 LTS (GNU/Linux 3.2.0-33-virtual x86_64)
 ...
tbatchelli@ip-10-80-187-174:~$
```

No password you ask? Nope, `learn-pallet` has authorized your SSH keys
on the node for your convenience. Go ahead and checkout that java is
installed in the node by running:
```shell
tbatchelli@ip-10-80-187-174:~$ java -version

java version "1.7.0_21"
OpenJDK Runtime Environment (IcedTea 2.3.9) (7u21-2.3.9-0ubuntu0.12.04.1)
OpenJDK 64-Bit Server VM (build 23.7-b01, mixed mode)
```

Ok, now that we can remove our node. We do so from the Clojure REPL
again:

```clojure
install-java=> (def result (destroy))
```

And if we wanted to go to another exercise, we'd use `switch-ns` again:

```clojure
install-java=> (switch-ns <new-exercise>)
```

## Contact

If you run into issues running `learn-pallet`, or have
suggestions, or just want to chat, we'd be happy to help you. Please
contact us via any of these options:

 - Join the [Pallet mailing list][pallet-ml] and send a message
 - Join the #pallet channel on [FreeNode's IRC][freenode]
 - Create a new issue (feat request, bug, etc.) on
   [`learn-pallet`'s github repo][learn-pallet-issues]
 - Tweet [@palletops][palletops-tweet]

## License

Copyright © 2013 Antoni Batchelli

Distributed under the Eclipse Public License, the same as Clojure.

[java]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[jclouds]:
http://www.jclouds.org/documentation/reference/supported-providers/
[lein]: http://leiningen.org/#install
[lein-win]: http://leiningen-win-installer.djpowell.net
[vbox]: https://virtualbox.org
[vbox-dl]: https://www.virtualbox.org/wiki/Downloads
[vmfest]: https://github.com/tbatchelli/vmfest

[pallet-ml]: https://groups.google.com/forum/?fromgroups#!forum/pallet-clj
[freenode]: http://freenode.net/irc_servers.shtml
[learn-pallet-issues]: https://github.com/pallet/learn-pallet/issues
[palletops-tweet]: https://twitter.com/palletops
