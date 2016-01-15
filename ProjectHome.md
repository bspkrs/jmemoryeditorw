# Introduction #

I created this program to allow myself to hack game memory for cheating purposes - increase money for example. See [UserGuide](UserGuide.md) about how to use the program.

Originally, I developed a Delphi based solution several years ago, but was a bit feature less for my current need, and as I'm now a Java developer, took the challenge of re-implementing it in Java using the JNA technology.

It includes the [Java Native Access](https://jna.dev.java.net/) library from Sun and an earlier version of [Google Collections](http://code.google.com/p/google-collections/).

I think, by itself it is well suited example for how to program JNA and how to use it to call interresting Windows functions: Get an image for an executable, get the current processes etc.

New utility: [HogClicker](HogClicker.md).

Version 2.2 contains an updated JNA library for better expected stability.

Version 2.1 contains a small bug fix: filtering by decreased values did not work correctly.

# Installation #
Download and run the jar(s).

`java -jar jmemoryeditorw-2.2.jar`

`java -jar hogclicker-0.1.jar`

# System Requirements #
  * Java 1.6+
  * 64MB memory or more (for larger datasets you might need to use the `-Xmx128M` option)
  * Windows XP SP2 and upward. Vista and 7 requires elevation.
  * 2MB disk space for the jar and the state-save config file

# Screenshot #

![http://karnokd.uw.hu/jmemoryeditorw.png](http://karnokd.uw.hu/jmemoryeditorw.png)

![http://jmemoryeditorw.googlecode.com/svn/trunk/JMemEditorW/doc/HogClicker-0.1.png](http://jmemoryeditorw.googlecode.com/svn/trunk/JMemEditorW/doc/HogClicker-0.1.png)