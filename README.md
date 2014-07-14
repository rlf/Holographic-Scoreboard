Holographic-Scoreboard
======================

[Minecraft Bukkit Plugin][hgs] that allows for easy score-board generation using the [Holographic Displays][hd].

Compiling
---------

Download the API for HolographicDisplays, and place it in ```/src/main/lib/HolographicDisplays.jar```

Run the following commad:
```
 mvn install
```
A jar-file should be generated, and placed in the /target folder.

Installing
----------
Copy the jar-file to your bukkits /plugins folder, and either restart the server, or use ```plugman``` to reload the plugin.

 [hd]: http://dev.bukkit.org/bukkit-plugins/holographic-displays/ "Holographic Displays"
 [hgs]: http://dev.bukkit.org/bukkit-plugins/holographic-scoreboard/ "Holographic Scoreboard"
