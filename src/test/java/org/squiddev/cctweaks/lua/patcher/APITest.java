package org.squiddev.cctweaks.lua.patcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class APITest {
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
		System.setProperty("cctweaks.APIs.bit", "true");
		loader = VersionHandler.getLoader(version);
	}

	@Test
	public void testBitop() throws Throwable {
		VersionHandler.runFile(loader, "bitop");
	}

	@Test
	public void testBigInt() throws Throwable {
		VersionHandler.runFile(loader, "bigint");
	}
}
