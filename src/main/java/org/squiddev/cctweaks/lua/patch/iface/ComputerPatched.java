package org.squiddev.cctweaks.lua.patch.iface;

import dan200.computercraft.api.filesystem.IMount;

/**
 * Methods which are patched onto {@link dan200.computercraft.core.computer.Computer}. You can safely cast to this.
 */
public interface ComputerPatched {
	/**
	 * Determine whether a computer is on or is starting
	 *
	 * @return If the computer is on
	 */
	boolean isMostlyOn();

	/**
	 * If this computer should no longer handle events
	 *
	 * @return If this computer should not resume
	 * @see IComputerEnvironmentExtended#suspendEvents()
	 */
	boolean suspendEvents();

	/**
	 * Set a custom mount for this computer
	 *
	 * @param biosPath The custom bios path to use
	 * @param mount    The custom mount to use
	 */
	void setRomMount(String biosPath, IMount mount);
}
