package org.squiddev.cctweaks.api.lua;

import dan200.computercraft.core.lua.ILuaMachine;

/**
 * An extended version of a {@link ILuaMachine}, which provides more functionality.
 */
public interface IExtendedLuaMachine extends ILuaMachine {
	/**
	 * Set a global variable
	 *
	 * @param name   The global variable to set
	 * @param object The value to set it to
	 */
	void setGlobal(String name, Object object);

	/**
	 * Set this machine to debugging mode. Generally this will inject Lua's debug API.
	 */
	void enableDebug();
}
