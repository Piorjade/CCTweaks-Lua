package org.squiddev.cctweaks.lua;

import org.squiddev.cctweaks.lua.lib.socket.AddressMatcher;
import org.squiddev.cobalt.luajc.CompileOptions;
import org.squiddev.configgen.*;
import org.squiddev.patcher.Logger;

/**
 * The main config class
 */
@org.squiddev.configgen.Config(languagePrefix = "gui.config.cctweaks.", propertyPrefix = "cctweaks")
public final class Config {
	public static String mcVersion = "1.8.9";

	private static final String BIOS_PATH = "/assets/computercraft/lua/bios.lua";

	@OnSync
	public static void sync() {
		if (!Computer.biosPath.startsWith("/")) {
			Logger.warn("Bios path (" + Computer.biosPath + ") does not start with '/', reverting to default");
			Computer.biosPath = BIOS_PATH;
		} else if (Config.class.getResource(Computer.biosPath) == null) {
			Logger.warn("Cannot find custom bios (" + Computer.biosPath + "), reverting to default");
			Computer.biosPath = BIOS_PATH;
		}

		if (Computer.MultiThreading.enabled && Computer.MultiThreading.threads > 1 && !Computer.cobalt && !Computer.rembulan) {
			Logger.warn("Can only have 1 thread when running on LuaJ, reverting to default");
			Computer.MultiThreading.threads = 1;
		}

		if (Computer.luaJC) {
			Computer.LuaJC.enabled = true;
			Logger.warn("Computer.luaJC is deprecated: use Computer.LuaJC.enabled instead");
		}

		if (Computer.luaJCVerify) {
			Computer.LuaJC.verify = true;
			Logger.warn("Computer.luaJCVerify is deprecated: use Computer.LuaJC.verify instead");
		}
	}

	/**
	 * Computer tweaks and items.
	 */
	public static final class Computer {
		/**
		 * Time in milliseconds before 'Too long without yielding' errors.
		 * You cannot shutdown/reboot the computer during this time.
		 * Use carefully.
		 */
		@DefaultInt(7000)
		@Range(min = 0)
		public static int computerThreadTimeout;

		/**
		 * Compile Lua bytecode to Java bytecode.
		 * Deprecated: Use Computer.LuaJC.enabled instead!
		 */
		@DefaultBoolean(false)
		@RequiresRestart(mc = false, world = true)
		@Deprecated
		public static boolean luaJC;

		/**
		 * Verify LuaJC sources on generation.
		 * Deprecated: Use Computer.LuaJC.verify instead!
		 */
		@DefaultBoolean(false)
		@Deprecated
		public static boolean luaJCVerify;

		/**
		 * Use the Cobalt Lua engine instead.
		 * This is a fork of LuaJ with many bugs fixed.
		 * However other bugs may have appeared, so use with caution.
		 */
		@DefaultBoolean(false)
		public static boolean cobalt;

		/**
		 * Use the Rembulan Lua 5.1 engine instead.
		 * This will require a custom BIOS and many programs may not work as expected
		 */
		@DefaultBoolean(false)
		public static boolean rembulan;

		/**
		 * Error much earlier on a timeout.
		 * Note: This only applies to the Cobalt VM
		 */
		@DefaultBoolean(false)
		public static boolean timeoutError;

		/**
		 * Specify a custom bios path to use. You
		 * must include the initial slash.
		 */
		@RequiresRestart(mc = false, world = true)
		@DefaultString(BIOS_PATH)
		public static String biosPath;

		/**
		 * Maximum number of file handles a single computer can have open
		 */
		@DefaultInt(1024)
		@Range(min = 1)
		public static int maxFilesHandles;

		/**
		 * Compile Lua bytecode to Java bytecode.
		 * This speeds up code execution.
		 */
		public static class LuaJC {
			/**
			 * Compile Lua bytecode to Java bytecode.
			 * This speeds up code execution.
			 */
			@DefaultBoolean(false)
			@RequiresRestart(mc = false, world = true)
			public static boolean enabled;

			/**
			 * Verify sources on generation.
			 * This will slow down compilation.
			 * If you have errors, please turn this and debug on and
			 * send it with the bug report.
			 */
			@DefaultBoolean(false)
			public static boolean verify;

			/**
			 * Number of calls required before compiling:
			 * 1 compiles when first called,
			 * 0 or less compiles when loaded
			 */
			@DefaultInt(CompileOptions.THRESHOLD)
			@Range(min = 0)
			public static int threshold;
		}

		/**
		 * Configuration options to enable running computers across multiple
		 * threads.
		 */
		@RequiresRestart
		public static class MultiThreading {
			/**
			 * Whether the custom multi-threaded executor is enabled.
			 * This can be used with any runtime but may function differently
			 * to normal ComputerCraft.
			 */
			@DefaultBoolean(false)
			public static boolean enabled;

			/**
			 * Number of threads to execute computers on. More threads means
			 * more computers can run at once, but may consume more resources.
			 * This requires the Cobalt VM.
			 */
			@DefaultInt(1)
			@Range(min = 1)
			public static int threads;
		}
	}

	/**
	 * Custom APIs for computers
	 */
	public static final class APIs {
		/**
		 * TCP connections from the socket API
		 */
		public static final class Socket {
			/**
			 * Enable TCP connections.
			 * When enabled, the socket API becomes available on
			 * all computers.
			 */
			@DefaultBoolean(true)
			@RequiresRestart(mc = false, world = true)
			public static boolean enabled;

			/**
			 * Blacklisted domain names.
			 *
			 * Entries are either domain names (www.example.com) or IP addresses in
			 * string format (10.0.0.3), optionally in CIDR notation to make it easier
			 * to define address ranges (1.0.0.0/8). Domains are resolved to their
			 * actual IP once on startup, future requests are resolved and compared
			 * to the resolved addresses.
			 */
			@DefaultString({"127.0.0.0/8", "10.0.0.0/8", "192.168.0.0/16", "172.16.0.0/12"})
			public static AddressMatcher blacklist;

			/**
			 * Whitelisted domain names.
			 * If something is mentioned in both the blacklist and whitelist then
			 * the blacklist takes priority.
			 */
			public static AddressMatcher whitelist;

			/**
			 * Maximum TCP connections a computer can have at any time
			 */
			@DefaultInt(4)
			@Range(min = 1)
			public static int maxTcpConnections;

			/**
			 * Number of threads to use for processing name lookups.
			 */
			@DefaultInt(4)
			@Range(min = 1)
			@RequiresRestart
			public static int threads;

			/**
			 * Maximum number of characters to read from a socket.
			 */
			@DefaultInt(2048)
			@Range(min = 1)
			public static int maxRead = 2048;
		}

		/**
		 * Basic data manipulation
		 */
		public static final class Data {
			/**
			 * If the data API is enabled
			 */
			@DefaultBoolean(true)
			@RequiresRestart(mc = false, world = true)
			public static boolean enabled;

			/**
			 * Maximum number of bytes to process.
			 * The default is 1MiB
			 */
			@DefaultInt(1048576)
			public static int limit;
		}

		/**
		 * Enable the debug API.
		 * This is NOT recommended for servers, use at your own risk.
		 * It should be save on servers if using Cobalt though.
		 */
		@RequiresRestart(mc = false, world = true)
		public static boolean debug;

		/**
		 * Enable the profiler API.
		 * Only works on Cobalt
		 */
		@RequiresRestart(mc = false, world = true)
		public static boolean profiler;

		/**
		 * Enable the biginteger API.
		 */
		@RequiresRestart(mc = false, world = true)
		@DefaultBoolean(true)
		public static boolean bigInteger;

		/**
		 * Enable the extended bit operator library
		 */
		@RequiresRestart(mc = false, world = true)
		@DefaultBoolean(true)
		public static boolean bitop;
	}

	/**
	 * Only used when testing and developing the mod.
	 * Nothing to see here, move along...
	 */
	public static final class Testing {
		/**
		 * Show debug messages.
		 * If you hit a bug, enable this, rerun and send the log
		 */
		@DefaultBoolean(false)
		public static boolean debug;

		/**
		 * Throw exceptions on calling deprecated methods
		 *
		 * Only for development/testing
		 */
		@DefaultBoolean(false)
		public static boolean deprecatedWarnings;

		/**
		 * Dump the modified class files to asm/CCTweaks
		 */
		@DefaultBoolean(false)
		public static boolean dumpAsm;
	}
}
