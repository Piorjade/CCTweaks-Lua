package org.squiddev.cctweaks.lua.patcher;

import org.junit.Test;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.asm.WhitelistDebug;

public class WhitelistDebugTest {
	@Test
	public void assertWorks() throws Throwable {
		RewriteClassLoader loader = new RewriteClassLoader(new WhitelistDebug());
		Config.Computer.debug = true;

		loader.run("assert.assert(debug, 'Expected debug API')");
	}
}
