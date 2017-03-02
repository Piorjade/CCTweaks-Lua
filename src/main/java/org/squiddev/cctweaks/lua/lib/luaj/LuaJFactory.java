package org.squiddev.cctweaks.lua.lib.luaj;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.lua.LuaJLuaMachine;
import org.luaj.vm2.LuaTable;
import org.squiddev.cctweaks.api.lua.ILuaMachineFactory;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.patcher.Logger;

import java.lang.reflect.Field;

public class LuaJFactory implements ILuaMachineFactory<LuaJLuaMachine> {
	@Override
	public String getID() {
		return "luaj";
	}

	@Override
	public LuaJLuaMachine create(Computer computer) {
		LuaJLuaMachine machine = new LuaJLuaMachine(computer);
		LuaTable env = null;
		try {
			env = (LuaTable) getGlobals.get(machine);
		} catch (IllegalAccessException e) {
			Logger.error("Cannot get LuaJLuaMachine.m_globals", e);
		}

		if (env != null) {
			if (Config.APIs.bigInteger) BigIntegerValue.setup(env);
			if (Config.APIs.bitop) BitOpLib.setup(env);
		}

		return machine;
	}

	@Override
	public boolean supportsMultithreading() {
		return false;
	}

	@Override
	public String getPreBios() {
		return PRE_BIOS_STRING;
	}

	private static Field getGlobals = null;

	static {
		try {
			getGlobals = LuaJLuaMachine.class.getDeclaredField("m_globals");
			getGlobals.setAccessible(true);
		} catch (NoSuchFieldException e) {
			Logger.error("Cannot load LuaJLuaMachine.m_globals", e);
		}
	}
}
