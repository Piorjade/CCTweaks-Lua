package org.squiddev.cctweaks.lua.asm;

import org.squiddev.cctweaks.lua.asm.binary.BinaryUtils;

/**
 * Setup everything
 */
public class Tweaks {
	public static void setup(CustomChain chain) {
		chain.addPatchFile("org.luaj.vm2.lib.DebugLib");
		chain.addPatchFile("org.luaj.vm2.lib.StringLib");

		chain.add(new AddAdditionalData());
		chain.add(new CustomAPIs());
		chain.add(new CustomMachine());
		chain.add(new CustomTimeout());
		chain.add(new InjectLuaJC());
		chain.add(new WhitelistGlobals());
		BinaryUtils.inject(chain);
	}
}
