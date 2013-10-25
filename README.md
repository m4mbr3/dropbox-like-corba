dropbox-like-corba
==========================

Dropboxlike application developed in java and corba


For another client  (written in erlang and corba) look for the project dropbox-like-client-erlang
at https://github.com/m4mbr3/dropbox-like-client-erlang



How Install it?
---------------------------
###Requirements:

1. [java] (https://www.java.com/it/download/)
2. [javac] (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
3. [jacORB with sources] (http://www.jacorb.org/download.html)
4. [git] (http://git-scm.com/)

###Steps:

1. Install java and javac for your OS.
2. Download and extract jacORB archive where you want into the filesystem.
3. Include the bin directory inside the jacORB extracted folder in your PATH
    `export PATH=$PATH:/path/to/your/jacorb/bin/folder`
4. Install git on your system following the instruction on the website
5. Clone the repository using
    `git clone https://github.com/m4mbr3/dropbox-like-corba.git`
wherever you want on the filesystem (to decide the name of the folder add at the end of the command above your preference).
6. Open the `src/Makefile` and change the classpath of the command to point to the exact sources directory of corba.  
####For Example:
    If the main jacorb directory is  `/home/myuser/jacorb_*_*/`  
    You have to change the -classpath into `/home/myuser/jacorb_*_*/src/omg-*-*-*/` for all the command into the Makefile.
7. Now run it using the command `make` inside the `src` folder

Configuration
-------------------------
To configure the dropboxlike application you have to edit the config.sh file  

1. `DROPBOXLIKE_HOME` is the environment variable of the parent directory where the files of the server will be stored  
For instance: you can modify it to point to your home directory using `$HOME` or specify something like `/my/folder` where you have to be able to write
The program automagically appends to that path the name `dropboxlike` which will be the main directory of the server.
2. `DROPBOXLIKECLIENT_HOME` referes to the location where all the client information will be stored  
For instance: you can modify it to point to your home directory using `$HOME` or specify something like `/my/folder` where you have to be able to write
The program automagically appends to that path the name `dropboxlikeclient` which will be the main directory of the client.
3. `DROPBOXLIKESERVERIP` is used by the erlang client only. If it is not setted will be used localhost instead. For the java client doesn't care.

and then run, from the directory `src` the command  
`./config.sh`

How run it?
-------------------------

### Run the service orbd

To run the orbd deamon you have to launch the command  
    `orbd -ORBInitialPort 1050 &`

### Run the server

To run the dropboxlike server move from `src` directory  into the `build/src` subdir  
`user@path/to/project/src/ $ cd build/src`  
`user@path/to/project/src/build/src/ $`  

then launch the command  
    `java src.DropboxLikeServer -ORBInitialPort 1050  -ORBInitialHost localhost &`

### Run the java client

To run the dropboxlike client from the same folder above run the command :

`java src.DropboxLikeClient -ORBInitialPort 1050  -ORBInitialHost localhost`

NB: This program is supposed to work in a real distributed environment so your server may be running on a different machine.  
If it is the case please modify the localhost with the right address.


### Run the erlang client

To run the dropboxlike client written in erlang you have to recover from the server using the following commands:  

`git submodule init`
`git submodule update`

You will finally find the client code at the path `src/client-erlang/`  
To have it ready to use please follow the [instruction](https://github.com/m4mbr3/dropbox-like-client-erlang).
