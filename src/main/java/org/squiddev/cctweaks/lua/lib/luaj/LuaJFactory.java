package org.squiddev.cctweaks.lua.lib.luaj;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.lua.LuaJLuaMachine;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.DebugLib;
import org.squiddev.cctweaks.api.lua.IExtendedLuaMachine;
import org.squiddev.cctweaks.api.lua.ILuaMachineFactory;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.patcher.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class LuaJFactory implements ILuaMachineFactory<LuaJFactory.LuaJMachine> {
	@Override
	public String getID() {
		return "luaj";
	}

	@Override
	public LuaJMachine create(Computer computer) {
		LuaJMachine machine = new LuaJMachine(computer);

		LuaTable env = machine.getGlobals();
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

	public static class LuaJMachine extends LuaJLuaMachine implements IExtendedLuaMachine {
		private static Field getGlobals = null;
		private static Method toValue = null;

		static {
			try {
				getGlobals = LuaJLuaMachine.class.getDeclaredField("m_globals");
				getGlobals.setAccessible(true);
			} catch (NoSuchFieldException e) {
				Logger.error("Cannot load LuaJLuaMachine.m_globals", e);
			}

			try {
				toValue = LuaJLuaMachine.class.getDeclaredMethod("toValue", Object.class);
				toValue.setAccessible(true);
			} catch (NoSuchMethodException e) {
				Logger.error("Cannot load LuaJLuaMachine.toValue", e);
			}
		}

		public LuaJMachine(Computer computer) {
			super(computer);
		}

		public LuaTable getGlobals() {
			try {
				return (LuaTable) getGlobals.get(this);
			} catch (IllegalAccessException e) {
				Logger.error("Cannot get LuaJLuaMachine.m_globals", e);
				return null;
			}
		}

		@Override
		public void setGlobal(String name, Object object) {
			LuaTable globals = getGlobals();
			if (globals != null) {
				LuaValue converted;
				try {
					converted = (LuaValue) toValue.invoke(this, object);
				} catch (Exception e) {
					Logger.error("Cannot call LuaJLuaMachine.toValue", e);
					converted = LuaValue.NIL;
				}

				globals.rawset(name, converted);
			}
		}

		@Override
		public void enableDebug() {
			LuaTable env = getGlobals();
			if (env != null) {
				env.load(new DebugLib());
			}
		}
	}
}
