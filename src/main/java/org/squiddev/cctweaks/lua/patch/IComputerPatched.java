package org.squiddev.cctweaks.lua.patch;

/**
 * Extensions to {@link dan200.computercraft.core.computer.Computer}.
 */
public interface IComputerPatched {
	/**
	 * If this computer should no longer handle events
	 *
	 * @return If this computer should not resume
	 */
	boolean suspendEvents();
}
