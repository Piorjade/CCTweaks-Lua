# CCTweaks Lua
Modifications to ComputerCraft

This is a standalone version of [CCTweaks](https://github.com/SquidDev-CC/CC-Tweaks) that just contains modifications to the LuaVM and surrounding infrastructure.

## Getting started
 - [Download the latest release](https://github.com/SquidDev-CC/CCTweaks-Lua/releases/latest)
 - Add that and [Guava](https://github.com/google/guava) to your class path
 - Run `java -cp cctweaks.jar:guava.jar org.squiddev.cctweaks.lua.launch.Launcher [actual main class and arguments...]`
 
This will inject CCTweaks into the class loader, adding modifications.

## Features
 - Custom computer timeout
 - Whitelist globals (such as debug)
 - Fix the binary strings (for fs, http, rednet and os.queueEvent)
 - TCP socket API (`socket`)
 - Compression API (`data`)
 - [LuaJC](https://github.com/SquidDev/luaj.luajc) compiler (compiles Lua code to Java for performance boost)
 - [Cobalt](https://github.com/SquidDev/Cobalt) VM (reentrant fork of LuaJ)
   - Custom termination handler
   - Several bugs fixed (any object error messages, string pattern matching, number format strings)
 - Return HTTP handle on failures
 - Allow getting headers from HTTP responses
 - API for adding custom APIs

## Tweaking
Most tweaks are applied by default, though some can be configured through the command line:
 - `-Dcctweaks.Computer.debug=true`: Enable the debug API.
 - `-Dcctweaks.Computer.luaJC=true`: Enable LuaJC compilation (a `luaJC` flag also exists).
 - `-Dcctweaks.Computer.cobalt=true`: Enable the [Cobalt VM](https://github.com/SquidDev/Cobalt).
 - `-Dcctweaks.Computer.computerThreadTimeout=7000`: Set the computer thread timeout. Time is measured in milliseconds.
 - `-Dcctweaks.Computer.timeoutError=true`: Enable timeouts anywhere, rather than just from CC functions.
 - `-Dcctweaks.APIs.Socket.blacklist=127.0.0.0/8,10.0.0.0/8`: Configure the socket blacklist (can also do `whitelist`).

## CCEmuRedux
This project is partly aimed at CCEmuRedux and so contains a custom launcher for CCEmuRedux.

 - Place the jar in `.ccemuredux/bin/`
 - Change directory to `.ccemuredux`
 - Execute `java -cp "bin/*" org.squiddev.cctweaks.lua.launch.CCEmuRedux`

Several configuration options also exist:

 - `-Dcctweaks.ccemu.width=51`: Manually set the width of all computers
 - `-Dcctweaks.ccemu.height=19`: Manually set the height of all computers

