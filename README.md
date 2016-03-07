# CCTweaks Lua
Modifications to ComputerCraft

This is a standalone version of [CCTweaks](https://github.com/SquidDev-CC/CC-Tweaks) that just contains modifications to the LuaVM and surrounding infrastructure.

## Getting started
 - [Download the latest release](https://github.com/SquidDev-CC/CCTweaks-Lua/releases/latest)
 - Add that and [Guava](https://github.com/google/guava) to your class path
 - Run `java -cp cctweaks.jar:guava.jar  java -Dcctweaks.debug=true -cp org.squiddev.cctweaks.lua.launch.Launcher [actual main class and arguments...]`
 
This will inject CCTweaks into the class loader, adding modifications.

## Tweaking
Most tweaks are applied by default, though some can be configured through the command line:
 - `-Dcctweaks.debug=true`: Enable the debug API.
 - `-Dcctweaks.luajc=true`: Enable LuaJC compilation (a `luajc.verify` flag also exists).
 - `-Dcctweaks.cobalt=true`: Enable the [Cobalt VM](https://github.com/SquidDev/Cobalt).
 - `-Dcctweaks.timeout=10000`: Set the computer thread timeout. Time is measured in milliseconds.
