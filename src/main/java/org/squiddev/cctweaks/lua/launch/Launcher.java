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

		if (parseProperty("cctweaks.debug")) {
			Config.Computer.globalWhitelist = new String[]{"debug"};
		}

		Config.Computer.luaJC = parseProperty("cctweaks.luajc");
		Config.Computer.luaJCVerify = parseProperty("cctweaks.luajc.verify");
		Config.Computer.cobalt = parseProperty("cctweaks.cobalt");

		String timeout = System.getProperty("cctweaks.timeout");
		if (timeout != null) Config.Computer.computerThreadTimeout = Integer.parseInt(timeout);

		Config.onSync();

		String klass = args[0];
		String[] remaining = Arrays.copyOfRange(args, 1, args.length);

		URLClassLoader current = (URLClassLoader) ClassLoader.getSystemClassLoader();
		RewritingLoader classLoader = new RewritingLoader(current.getURLs());
		Thread.currentThread().setContextClassLoader(classLoader);

		Tweaks.setup(classLoader.chain);
		classLoader.chain.finalise();

		classLoader.loadClass("org.squiddev.cctweaks.lua.lib.ApiRegister")
			.getMethod("init")
			.invoke(null);

		classLoader.loadClass(klass)
			.getMethod("main", String[].class)
			.invoke(null, new Object[]{remaining});
	}

	private static boolean parseProperty(String name) {
		String value = System.getProperty(name);
		return value != null && (value.equalsIgnoreCase("t") || value.equalsIgnoreCase("true"));
	}
}
