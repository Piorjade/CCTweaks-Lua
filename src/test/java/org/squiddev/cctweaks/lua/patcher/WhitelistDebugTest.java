package org.squiddev.cctweaks.lua.patcher;

import org.junit.Test;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;

public class WhitelistDebugTest {
	@Test
	public void assertWorks() throws Throwable {
		System.setProperty("cctweaks.Computer.debug", "true");

		RewritingLoader loader = VersionHandler.getLoader("1.78");
		VersionHandler.run(loader, "assert.assert(debug, 'Expected debug API')");
	}
}
