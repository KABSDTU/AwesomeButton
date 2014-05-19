AwesomeButton
=============

Quick and dirty client-server application to make it possible for
clients to get a server to play certain sound clips it has on disk.

Can currently be set up to control [Silverjuke](http://www.silverjuke.net/),
such that every time a sound clip is played on the server, it will
lower the volume of Silverjuke until the end of the sound.


Fork me
=======

Feel free to fork and extend the application with more awesome features. This was mostly coded in a few hours, so it could do with some improvements!


Getting started
===============

Following is a short introduction on how to get started with AwesomeButton.


Requirements
------------

1. A network which both the server and client(s) can connect to
(i.e. LAN or wireless)

2. [Java](https://www.java.com/en/download/) installed on the server

3. Currently only Android devices are supported as clients.

Sounds
------

1. Create a folder called `sounds` in the same folder as the `AwesomeButtonServer.jar` is located.

2. Put all the sound clips (`.wav` only) you want to be able to play in the `sounds` folder.

3. Create a file called `sounds.txt` in the `sounds` folder. In this file each line represents
a sound clip.

  * Each line has to be formatted as: `<code-for-sound>//<name-of-sound-file>`:

I.e. `runaway//RUN AWAY!`, where the file is called `RUN AWAY!.wav` while the code for the 
sound will become `runaway`. Keep the code for the sound clip simple (no whitespaces/symbols/uppercase), and the name of the sound file without the `.wav` ending.

A full setup including sounds and the server executable can be found [in this example](https://github.com/mktange/AwesomeButton/releases/download/1.0/ExampleSetup.zip)

Setup
-----

### Server:

1. Start the AwesomeButtonServer on the designated music computer by executing the [AwesomeButtonServer.jar](https://github.com/mktange/AwesomeButton/releases/download/1.0/AwesomeButtonMobile.apk) file

2. Note the IP the server which can be seen in the first line of the log window upon start-up.

3. If Silverjuke is to be used at the same time, click the `Find Silverjuke` button and locate `Silverjuke.exe` on your computer with the file finder.

### Client:

Get the AwesomeButton app by downloading the [AwesomeButtonMobile.apk](https://github.com/mktange/AwesomeButton/releases/download/1.0/AwesomeButtonMobile.apk) file onto your device and installing it. Note that you may need to give extra permissions to the application before you are allowed to install it.

Then to use it:

1. Be sure to connected to the same wireless/local network as the server.

2. Open the client app, and hit `Settings > Set Server IP`. Here enter the IP the server was given.

3. Now get the sound list on the client by hitting `Settings > Get sounds`

4. ???

5. PROFIT

