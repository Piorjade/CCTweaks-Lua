package org.squiddev.cctweaks.lua;

import org.squiddev.cctweaks.lua.lib.socket.AddressMatcher;
import org.squiddev.configgen.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The main config class
 */
// @org.squiddev.configgen.Config(languagePrefix = "gui.config.cctweaks.")
public final class Config {
	public static Set<String> globalWhitelist;

	public static AddressMatcher socketWhitelist;
	public static AddressMatcher socketBlacklist;

//	public static void init(File file) {
//		ConfigLoader.init(file);
//	}
//
//	public static void sync() {
//		ConfigLoader.sync();
//	}

	static {
		onSync();
	}

	@OnSync
	public static void onSync() {
		// Handle generation of HashSets, etc...
		globalWhitelist = new HashSet<String>(Arrays.asList(Computer.globalWhitelist));

		socketWhitelist = new AddressMatcher(APIs.Socket.whitelist);
		socketBlacklist = new AddressMatcher(APIs.Socket.blacklist);
	}

	/**
	 * Computer tweaks and items.
	 */
	public static final class Computer {
		/**
		 * Globals to whitelist (are not set to nil).
		 * This is NOT recommended for servers, use at your own risk.
		 */
		@RequiresRestart(mc = false, world = true)
		public static String[] globalWhitelist = new String[0];

		/**
		 * Time in milliseconds before 'Too long without yielding' errors.
		 * You cannot shutdown/reboot the computer during this time.
		 * Use carefully.
		 */
		@DefaultInt(7000)
		@Range(min = 0)
		public static int computerThreadTimeout = 7000;

		/**
		 * Compile Lua bytecode to Java bytecode.
		 * This speeds up code execution.
		 */
		@DefaultBoolean(false)
		@RequiresRestart(mc = false, world = true)
		public static boolean luaJC;

		/**
		 * Verify LuaJC sources on generation.
		 * This will slow down compilation.
		 * If you have errors, please turn this and debug on and
		 * send it with the bug report.
		 */
		@DefaultBoolean(false)
		public static boolean luaJCVerify;

		/**
		 * Use the Cobalt Lua engine instead.
		 * This is a fork of LuaJ with many bugs fixed.
		 * However other bugs may have appeared, so use with caution.
		 * This is incompatible with LuaJC.
		 */
		@DefaultBoolean(false)
		public static boolean cobalt;
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
			public static boolean enabled = true;

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
			public static String[] blacklist = new String[]{"127.0.0.0/8", "10.0.0.0/8", "192.168.0.0/16", "172.16.0.0/12"};

			/**
			 * Whitelisted domain names.
			 * If something is mentioned in both the blacklist and whitelist then
			 * the blacklist takes priority.
			 */
			public static String[] whitelist = new String[0];

			/**
			 * Maximum TCP connections a computer can have at any time
			 */
			@DefaultInt(4)
			@Range(min = 1)
			public static int maxTcpConnections = 4;

			/**
			 * Number of threads to use for processing name lookups.
			 */
			@DefaultInt(4)
			@Range(min = 1)
			@RequiresRestart
			public static int threads = 4;

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
			public static boolean enabled = true;

			/**
			 * Maximum number of bytes to process.
			 * The default is 1MiB
			 */
			@DefaultInt(1048576)
			public static int limit = 1048576;
		}
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
