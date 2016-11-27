package org.squiddev.cctweaks.lua.patcher;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;

import java.util.List;

@RunWith(Parameterized.class)
public class WhitelistDebugTest {
	@Parameterized.Parameters(name = "Version: {0}, Runtime: {1}")
	public static List<Object[]> getVersions() {
		return VersionHandler.getVersionsWithRuntimes();
	}

	@Parameterized.Parameter
	public String version;

	@Parameterized.Parameter(1)
	public VersionHandler.Runtime runtime;

	@After
	public void tearDown() {
		runtime.tearDown();
	}

	@Test
	public void assertWorks() throws Throwable {
		System.setProperty("cctweaks.APIs.debug", "true");
		runtime.setup();

		RewritingLoader loader = VersionHandler.getLoader(version);
		VersionHandler.run(loader, "assert.assert(debug, 'Expected debug API')");

		System.setProperty("cctweaks.APIs.debug", "false");
	}
}
