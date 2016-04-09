package org.squiddev.cctweaks.lua.patcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class BinaryTest {
	@Parameterized.Parameters(name = "Version: {0}, Cobalt: {1}")
	public static List<Object[]> getVersions() {
		return VersionHandler.getVersionsWithCobalt();
	}

	@Parameterized.Parameter(0)
	public String version;

	@Parameterized.Parameter(1)
	public String cobalt;

	private ClassLoader loader;

	@Before
	public void setup() throws Exception {
		System.setProperty("cctweaks.Computer.cobalt", cobalt);
		loader = VersionHandler.getLoader(version);
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
