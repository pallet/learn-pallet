# Learn Pallet

A project to help you learn pallet via live-coding.

## Prerequisites

You need to have a Linux or Mac OSX computer, and if you are going to
use Amazon EC2, then you can also use a modern Windows OS.

You also your computer to have installed:

  - [Java 7][java]
  - [Leiningen][lein] (Use [this link][lein-win] for Windows)
  - A SSH key pair in a `.ssh` directory in your home directory, named
    `id_rsa` and `id_rsa.pub`
  - (Optional) If you want to run learn-pallet on local VMs, you need
    [VirtualBox 4.12 or newer][vbox]
    
## Backend Options

Pallet can use multiple backends. It can use any cloud provider, local
virtual machines, or your own servers and for simplicity,
`learn-pallet` is configured to run only on Amazon EC2 as a public
cloud and VirtualBox for local VMs.

Pallet has two methods for to connectoin to VirtualBox: XPCOM and
Web Services. The XPCOM method is more convenient and works well
on OSX and relatively old versions of Linux, but there are some open
issues that won't let XPCOM work with the latest versions of
Debian/Ubuntu, and there is no support for XPCOM on Windows. Besides,
we don't support VirtualBox on Windows yet. As a summary:

- __OSX, Linux (except Ubuntu 11.x or newer, Debian 6 or newer)__: use
  VirtualBox via XPCOM (or EC2)
- __Latest versions of Ubuntu or Debian__: use VirtualBox via
  Web Services (or EC2)
- __Windows__: use EC2

## Usage

To use `learn-pallet` you will need to initiate a Clojure REPL. All
the exercises are run at the REPL.

### Starting A Session
Before you can use any of the code, you need to bootstrap your
session. This will download some libraries and add them to this
project.

Open a terminal and change the current directory to where
`learn-pallet` is, and then run `lein repl` to get the Clojure REPL:

```shell
bash$ lein repl
 ...
user=>
```

Notice that the REPL prompt is `user=>` for the default `user`
namespace. 

Now, at the REPL, we need to load `learn-pallet` and bootstrap the
session:

```clojure
user=> (use 'learn-pallet)
nil
```

You're ready to bootstrap the system to use EC2 or VirtualBox.

### Bootstrapping for VirtualBox (XPCOM)

At the REPL, run:

```clojure
user=> (bootstrap :vmfest)

*** Congratulations! Your setup already contains an image with id :ubuntu-12.04
*** This means we're ready to roll :)
nil
```

This will get you a working VMFest if you have VirtualBox already
installed. If you don't have an Ubuntu 12.04 image installed, it will
download and install one, so be patient! Once this comes back, you're
ready for your session, and you can skip to "Running the Exercises".

### Bootstrapping for Amazon EC2

At the REPL, run: 

```clojure
user=> (bootstrap :ec2 :identity "<your-aws-identity>" :credential "<your-aws-credential>")

*** Congratulations! You're connected to Amazon Web Services. Enjoy!
nil
```

This will log you into Amazon AWS. Once this comes back, you're ready
for your session, and you can skip to "Running the Exercises".

## Running the Exercises

Each exercise in `learn-pallet` lives in its own namespace. We will
use `switch-ns` to switch exercises (namespaces). E.g. to run the code
for the `install-java` exercise we do:

```clojure
user=> (switch-ns install-java)
  ...
install-java=>
```

Running this command will download and install a few libraries, so it
might take some time to come back. Please disregard the WARN messages,
they are just letting you know the about the magic `learn-pallet` is
performing on your JVM's classpath, and these messages will go away in
the future. Also, you might get other messages if the system had to
download libraries.

Notice that now your REPL is now on a different namespace than before:
`install-java`.

This particular exercise has a `run` function that will create a new
VM and get java installed on it.

```clojure
install-java=> (def result (run))
```

The output of running this function will be stored in the variable
`result`. Feel free to inspect this variable, e.g. `(pprint result)`.
Also, Pallet logs all the actions it peforms in the file
`logs/pallet.log` in your `learn-pallet` directory.

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

No password you ask? Nope, `learn-pallet` has autorized your SSH keys
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

## FAQ et al.

### Bootstrapping for VirtualBox with Web Services

For Pallet to connect to VirtualBox via Web Sevices, you need to run
__only once__ the following command at the shell:

```shell
bash$ VBoxManage setproperty websrvauthlibrary null
```

Every time you want to use Pallet with VMFest you will have to start
the VirtualBox server by running this at the shell and leave it running:

```shell
bash$ vboxwebsrv -t0
```

Now you can bootstrap vmfest at the REPL by running:

```clojure
user=> (bootstrap :vmfest-ws)
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
