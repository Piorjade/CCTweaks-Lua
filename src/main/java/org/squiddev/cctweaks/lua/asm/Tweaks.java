package org.squiddev.cctweaks.lua.asm;

import org.squiddev.cctweaks.lua.asm.binary.BinaryFS;
import org.squiddev.cctweaks.lua.asm.binary.BinaryMachine;
import org.squiddev.patcher.transformer.ClassMerger;
import org.squiddev.patcher.transformer.ClassReplacer;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.transformer.ISource;

/**
 * Setup everything
 */
public class Tweaks {
	public static void setup(CustomChain chain) {
		chain.addPatchFile("org.luaj.vm2.lib.DebugLib");
		chain.addPatchFile("org.luaj.vm2.lib.StringLib");

		chain.add(new AddAdditionalData());
		chain.add(new AddMethodDescriptor());
		chain.add(new CustomAPIs());
		chain.add(new CustomBios());
		chain.add(new CustomMachine());
		addMulti(chain, new CustomThreading());
		chain.add(new CustomTimeout());
		chain.add(new LimitLabel());
		chain.add(new WhitelistDebug());

		chain.add(new ClassMerger(
			"dan200.computercraft.core.computer.Computer",
			"org.squiddev.cctweaks.lua.patch.Computer_Patch"
		));

		chain.add(new ClassMerger(
			"dan200.computercraft.core.filesystem.FileSystem",
			"org.squiddev.cctweaks.lua.patch.FileSystem_Patch"
		));

		chain.add(new ClassMerger(
			"dan200.computercraft.core.apis.HTTPAPI",
			"org.squiddev.cctweaks.lua.patch.HTTPAPI_Patch"
		));

		// Binary patches
		chain.add(new BinaryMachine());
		chain.add(new ClassMerger("dan200.computercraft.core.apis.PeripheralAPI", "org.squiddev.cctweaks.lua.patch.PeripheralAPI_Patch"));
		chain.add(new ClassMerger("dan200.computercraft.core.apis.OSAPI", "org.squiddev.cctweaks.lua.patch.OSAPI_Patch"));
		chain.add(new ClassMerger("dan200.computercraft.shared.peripheral.modem.ModemPeripheral", "org.squiddev.cctweaks.lua.patch.ModemPeripheral_Patch"));

		// Binary FS patches
		chain.add(new BinaryFS());
		chain.add(new ClassMerger(BinaryFS.READER_OBJECT, BinaryFS.READER_OBJECT));
		chain.add(new ClassMerger(BinaryFS.WRITER_OBJECT, BinaryFS.WRITER_OBJECT));
		chain.add(new ClassMerger("dan200.computercraft.core.filesystem.FileSystem", "org.squiddev.cctweaks.lua.patch.binfs.FileSystem_Patch"));
	}

	private static void addMulti(CustomChain chain, ClassReplacer replacer) {
		chain.add((IPatcher) replacer);
		chain.add((ISource) replacer);
	}
}
