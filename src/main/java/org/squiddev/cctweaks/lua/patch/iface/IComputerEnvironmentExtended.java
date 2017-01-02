package org.squiddev.cctweaks.lua.patch.iface;

import dan200.computercraft.core.computer.IComputerEnvironment;

/**
 * Extensions to {@link IComputerEnvironment}. Not patched, but implemented by some patched classes.
 */
public interface IComputerEnvironmentExtended extends IComputerEnvironment {
	/**
	 * If this computer should no longer handle events
	 *
	 * @return If this computer should not resume
	 */
	boolean suspendEvents();
}
