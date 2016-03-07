package org.squiddev.cctweaks.lua.launch;

import org.squiddev.cctweaks.lua.asm.Tweaks;
import org.squiddev.cctweaks.lua.lib.ApiRegister;

import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * The main launcher
 */
public class Launcher {
	public static void main(String[] args) throws Exception {
		if (args.length == 0) System.err.println("Expected main class");

		String klass = args[0];
		String[] remaining = Arrays.copyOfRange(args, 1, args.length - 1);

		URLClassLoader current = (URLClassLoader) ClassLoader.getSystemClassLoader();
		RewritingLoader classLoader = new RewritingLoader(current.getURLs());
		Thread.currentThread().setContextClassLoader(classLoader);

		Tweaks.setup(classLoader.chain);
		new ApiRegister().init();
		classLoader.chain.finalise();

		classLoader.loadClass(klass)
			.getMethod("main", String[].class)
			.invoke(null, new Object[]{remaining});
	}
}
