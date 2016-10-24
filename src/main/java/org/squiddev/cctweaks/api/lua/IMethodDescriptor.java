package org.squiddev.cctweaks.api.lua;

import dan200.computercraft.api.lua.ILuaTask;

/**
 * Gives information about a particular method
 *
 * @see dan200.computercraft.api.lua.ILuaObject
 * @see dan200.computercraft.api.peripheral.IPeripheral
 */
public interface IMethodDescriptor {
	/**
	 * Determines whether a method method will ever yield control
	 *
	 * If this method returns true attempting to yield (through pulling events,
	 * executing main tasks or direct yielding) will result in an error.
	 *
	 * @param method The method on this object
	 * @return Whether this method will yield.
	 * @see dan200.computercraft.api.lua.ILuaContext#pullEvent(String)
	 * @see dan200.computercraft.api.lua.ILuaContext#pullEventRaw(String)
	 * @see dan200.computercraft.api.lua.ILuaContext#yield(Object[])
	 * @see dan200.computercraft.api.lua.ILuaContext#executeMainThreadTask(ILuaTask)
	 */
	boolean willYield(int method);
}
