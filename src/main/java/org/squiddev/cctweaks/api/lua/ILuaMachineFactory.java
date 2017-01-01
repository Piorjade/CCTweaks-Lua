package org.squiddev.cctweaks.api.lua;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.lua.ILuaMachine;

/**
 * Create a {@link dan200.computercraft.core.lua.ILuaMachine} for a computer;
 */
public interface ILuaMachineFactory<T extends ILuaMachine> {
	/**
	 * Get the ID of this machine
	 *
	 * @return The ID of this machine
	 */
	String getID();

	/**
	 * Create a machine for a given computer
	 *
	 * @param computer The computer to create this for
	 * @return The created machine.
	 */
	T create(Computer computer);

	/**
	 * Check if multi-threading is supported for this runtime.
	 *
	 * @return Whether this machine supports multi-threading.
	 */
	boolean supportsMultithreading();
}
