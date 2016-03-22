package org.squiddev.cctweaks.lua.patcher;

import org.junit.BeforeClass;
import org.junit.Test;

public class BinaryTest {
	private static ClassLoader loader;

	@BeforeClass
	public static void beforeClass() throws Exception {
		loader = VersionHandler.getLoader("1.78");
	}

	@Test
	public void queueEvent() throws Throwable {
		VersionHandler.runFile(loader, "binaryEvent");
	}

	@Test
	public void fileSystem() throws Throwable {
		VersionHandler.runFile(loader, "binaryFS");
	}

	@Test
	public void fileSystemDirect() throws Throwable {
		VersionHandler.runClass(loader, "runner.WrappedTests", "fsAPI");
	}
}
