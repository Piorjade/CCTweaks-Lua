package org.squiddev.cctweaks.lua.patcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;

import java.util.List;

@RunWith(Parameterized.class)
public class WhitelistDebugTest {
	@Parameterized.Parameters(name = "Version: {0}, Cobalt: {1}")
	public static List<Object[]> getVersions() {
		return VersionHandler.getVersionsWithCobalt();
	}

	@Parameterized.Parameter
	public String version;

	@Parameterized.Parameter(1)
	public String cobalt;

	@Test
	public void assertWorks() throws Throwable {
		System.setProperty("cctweaks.Computer.debug", "true");
		System.setProperty("cctweaks.Computer.cobalt", cobalt);

		RewritingLoader loader = VersionHandler.getLoader(version);
		VersionHandler.run(loader, "assert.assert(debug, 'Expected debug API')");

		System.setProperty("cctweaks.Computer.debug", "false");
	}
}
