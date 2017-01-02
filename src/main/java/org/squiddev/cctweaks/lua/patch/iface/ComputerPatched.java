package org.squiddev.cctweaks.lua.patch.iface;

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
}
