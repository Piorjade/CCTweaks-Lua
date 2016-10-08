package org.squiddev.cctweaks.lua.asm;

import org.objectweb.asm.ClassReader;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.patcher.transformer.ClassReplacer;

import java.io.IOException;

/**
 * Rewrites {@link dan200.computercraft.core.computer.ComputerThread} with
 * {@link org.squiddev.cctweaks.lua.patch.ComputerThread_Rewrite}.
 *
 * Only enabled if {@link org.squiddev.cctweaks.lua.Config.Computer.MultiThreading#enabled}
 * is true.
 */
public class CustomThreading extends ClassReplacer {
	public CustomThreading() {
		super(
			"dan200.computercraft.core.computer.ComputerThread",
			"org.squiddev.cctweaks.lua.patch.ComputerThread_Rewrite"
		);
	}

	@Override
	public boolean matches(String className) {
		return Config.Computer.MultiThreading.enabled && super.matches(className);
	}

	@Override
	public ClassReader getReader(String className) throws IOException {
		return Config.Computer.MultiThreading.enabled ? super.getReader(className) : null;
	}
}
