package org.squiddev.cctweaks.lua.patch;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.shared.peripheral.modem.ModemPeripheral;
import org.squiddev.cctweaks.api.lua.IArguments;
import org.squiddev.cctweaks.api.lua.IPeripheralWithArguments;

/**
 * Allows transmitting binary data. No casts are done so we can just delegate directly.
 */
public abstract class ModemPeripheral_Patch extends ModemPeripheral implements IPeripheralWithArguments {
	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, IArguments arguments) throws LuaException, InterruptedException {
		return callMethod(computer, context, method, arguments.asBinary());
	}
}
