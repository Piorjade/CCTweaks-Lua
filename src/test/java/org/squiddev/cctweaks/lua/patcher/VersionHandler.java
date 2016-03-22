package org.squiddev.cctweaks.lua.patcher;

import org.squiddev.cctweaks.lua.asm.Tweaks;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
			new Object[]{"1.79pr1"}
		);
	}

	public static RewritingLoader getLoader(String version) throws Exception {
		URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		URL[] urls = loader.getURLs();

		URL[] newUrls = new URL[urls.length + 1];
		System.arraycopy(urls, 0, newUrls, 1, urls.length);
		try {
			newUrls[0] = new File("lib/ComputerCraft-" + version + ".jar").toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		RewritingLoader newLoader = new RewritingLoader(newUrls);
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
