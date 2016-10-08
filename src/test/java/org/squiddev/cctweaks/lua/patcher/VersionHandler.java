package org.squiddev.cctweaks.lua.patcher;

import org.squiddev.cctweaks.lua.asm.Tweaks;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Handles different versions
 */
public class VersionHandler {
	public static List<Object[]> getVersions() {
		return Arrays.asList(
			new Object[]{"1.75"},
			new Object[]{"1.78"},
			new Object[]{"1.79"}
		);
	}

	public static List<Object[]> getVersionsWithRuntimes() {
		List<Object[]> versions = getVersions();
		List<Object[]> withRuntimes = new ArrayList<Object[]>(versions.size() * 3);
		for (Object[] version : versions) {
			{
				Object[] with = new Object[version.length + 1];
				System.arraycopy(version, 0, with, 0, version.length);
				with[with.length - 1] = "luaj";
				withRuntimes.add(with);
			}
			{
				Object[] without = new Object[version.length + 1];
				System.arraycopy(version, 0, without, 0, version.length);
				without[without.length - 1] = "cobalt";
				withRuntimes.add(without);
			}
			{
				Object[] without = new Object[version.length + 1];
				System.arraycopy(version, 0, without, 0, version.length);
				without[without.length - 1] = "rembulan";
				withRuntimes.add(without);
			}
		}

		return withRuntimes;
	}

	public static void setup(String runtime) {
		System.setProperty("cctweaks.Computer.cobalt", runtime.equals("cobalt") ? "true" : "false");
		System.setProperty("cctweaks.Computer.rembulan", runtime.equals("rembulan") ? "true" : "false");
	}

	public static RewritingLoader getLoader(String version) throws Exception {
		URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		URL[] urls = loader.getURLs();

		URL[] newUrls = new URL[urls.length + 1];
		System.arraycopy(urls, 0, newUrls, 0, urls.length);
		newUrls[urls.length] = new File("lib/ComputerCraft-" + version + ".jar").toURI().toURL();

		System.setProperty("cctweaks.Testing.dumpAsm", "true");

		RewritingLoader newLoader = new RewritingLoader(newUrls, new File("asm/cctweaks-" + version));
		newLoader.addClassLoaderExclusion("org.junit.");
		newLoader.addClassLoaderExclusion("org.hamcrest.");
		Tweaks.setup(newLoader.chain);
		newLoader.loadConfig();
		newLoader.chain.finalise();
		return newLoader;
	}

	public static void runClass(ClassLoader loader, String source, String name) throws Throwable {
		source = "org.squiddev.cctweaks.lua.patcher." + source;
		Class<?> runner = loader.loadClass(source);
		try {
			runner.getMethod(name).invoke(null);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	public static void run(ClassLoader loader, String source) throws Throwable {
		Class<?> runner = loader.loadClass("org.squiddev.cctweaks.lua.patcher.runner.RunOnComputer");
		try {
			runner.getMethod("run", String.class).invoke(null, source);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	public static void runFile(ClassLoader loader, String source) throws Throwable {
		Class<?> runner = loader.loadClass("org.squiddev.cctweaks.lua.patcher.runner.RunOnComputer");
		try {
			Scanner s = new Scanner(loader.getResourceAsStream(("org/squiddev/cctweaks/lua/patcher/" + source + ".lua"))).useDelimiter("\\A");
			runner.getMethod("run", String.class).invoke(null, s.hasNext() ? s.next() : "");
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
}
