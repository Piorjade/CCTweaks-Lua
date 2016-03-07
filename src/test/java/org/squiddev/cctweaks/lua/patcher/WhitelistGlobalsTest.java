package org.squiddev.cctweaks.lua.patcher;

import org.junit.Test;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.asm.WhitelistGlobals;

import java.util.HashSet;

public class WhitelistGlobalsTest {

	@Test
	public void assertWorks() throws Throwable {
		RewriteClassLoader loader = new RewriteClassLoader(new WhitelistGlobals());
		Config.globalWhitelist = new HashSet<String>();
		Config.globalWhitelist.add("debug");

		loader.run("assert.assert(debug, 'Expected debug API')");
	}
}
