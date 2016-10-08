package org.squiddev.cctweaks.lua.patcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class BinaryTest {
	@Parameterized.Parameters(name = "Version: {0}, Runtime: {1}")
	public static List<Object[]> getVersions() {
		return VersionHandler.getVersionsWithRuntimes();
	}

	@Parameterized.Parameter(0)
	public String version;

	@Parameterized.Parameter(1)
	public String runtime;

	private ClassLoader loader;

	@Before
	public void setup() throws Exception {
		VersionHandler.setup(runtime);
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
