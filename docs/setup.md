Setting Up Mendel
==================

The following tutorial will get a Mendel cluster up and running.

Prerequisites
-------------
* Java 8
* [Mendel](http://www.cs.colostate.edu/~ctolooee/Mendel)

Passwordless SSH
----------------

Managing the cluster requires some form of passwordless login for any reasonably-sized installation.

In general, the best way to accomplish this is to generate a public and private SSH key pair, and then distribute the public key to machines in the cluster you wish to run storage nodes on.  I would recommend using a password on your private key combined with *ssh-agent* to make life easier, although if you're on a secure installation you can probably skip the private key password.  Either way, consult your operating system's documentation, or see [Using ssh-agent with ssh](http://mah.everybody.org/docs/ssh) by Mark A. Hershberger to get you up and running.

Mendel Configuration
--------------------

### Property Configuration
The `conf/config.properties` file specifies two major properties:
* mendel.home.dir -- Mendel installation directory
* mendel.root.dir -- Where the file system root is stored. Files, journal, logs, etc... live here

All the property specifications are described in the `conf/config.properties` file with examples.

### Storage Nodes

You must specify the storage nodes that make up your cluster. The `conf/nodes` file contain a list of hosts to act as the storage nodes. The file contains host names or IP addresses, one per line, their storage node port number (if not set the default port will be used), and the group identifier (prefixed with a `$`). An example `nodes` file is shown below:
```
$ Group A
lattice-0:8989
lattice-1
lattice-2

$Group B
lattice-3
129.82.45.204:5055
129.82.46.59
```
