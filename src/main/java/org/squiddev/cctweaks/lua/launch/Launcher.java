package org.squiddev.cctweaks.lua.launch;

import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.asm.Tweaks;

import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * The main launcher
 */
public class Launcher {
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.err.println("Expected main class");
			System.exit(1);
		}

		setupConfig();
		RewritingLoader loader = setupLoader();
		loader.chain.finalise();
		execute(loader, args[0], Arrays.copyOfRange(args, 1, args.length));
	}

	public static void setupConfig() {
		if (parseBoolean("cctweaks.debug")) {
			Config.Computer.globalWhitelist = new String[]{"debug"};
		}

		Config.Computer.luaJC = parseBoolean("cctweaks.luajc");
		Config.Computer.luaJCVerify = parseBoolean("cctweaks.luajc.verify");
		Config.Computer.cobalt = parseBoolean("cctweaks.cobalt");

		String timeout = System.getProperty("cctweaks.timeout");
		if (timeout != null) Config.Computer.computerThreadTimeout = Integer.parseInt(timeout);

		Config.onSync();
	}

	public static RewritingLoader setupLoader() {
		URLClassLoader current = (URLClassLoader) ClassLoader.getSystemClassLoader();
		RewritingLoader classLoader = new RewritingLoader(current.getURLs());
		Thread.currentThread().setContextClassLoader(classLoader);

		Tweaks.setup(classLoader.chain);
		return classLoader;
	}

	public static void execute(ClassLoader classLoader, String className, String[] arguments) throws Exception {
		classLoader.loadClass("org.squiddev.cctweaks.lua.lib.ApiRegister")
			.getMethod("init")
			.invoke(null);

		classLoader.loadClass(className)
			.getMethod("main", String[].class)
			.invoke(null, new Object[]{arguments});
	}

	private static boolean parseBoolean(String name) {
		String value = System.getProperty(name);
		return value != null && Boolean.parseBoolean(value);
	}

	public static Integer parseNumber(String key) {
		String value = System.getProperty(key);
		if (value == null) return null;

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Cannot parse " + key + ": " + e.getMessage());
		}
	}
}
