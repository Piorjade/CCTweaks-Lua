package org.squiddev.cctweaks.lua.lib;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.lua.ILuaMachine;
import dan200.computercraft.core.lua.LuaJLuaMachine;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;
import org.objectweb.asm.ClassVisitor;
import org.squiddev.cctweaks.api.lua.ArgumentDelegator;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.lib.cobalt.CobaltMachine;
import org.squiddev.cctweaks.lua.lib.luaj.BigIntegerValue;
import org.squiddev.cctweaks.lua.lib.luaj.BitOpLib;
import org.squiddev.cctweaks.lua.lib.luaj.LuaJArguments;
import org.squiddev.cctweaks.lua.lib.rembulan.RembulanMachine;
import org.squiddev.patcher.Logger;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

/**
 * Various classes for helping with Lua conversion
 */
public class LuaHelpers {
	/**
	 * Simple method which creates varargs and delegates to the delegator. (I know how stupid that sounds).
	 *
	 * This exists so I don't have to grow the the stack size.
	 *
	 * @see org.squiddev.cctweaks.lua.asm.binary.BinaryMachine#patchWrappedObject(ClassVisitor)
	 */
	public static Object[] delegateLuaObject(ILuaObject object, ILuaContext context, int method, Varargs arguments) throws LuaException, InterruptedException {
		return ArgumentDelegator.delegateLuaObject(object, context, method, new LuaJArguments(arguments));
	}

	/**
	 * Wraps an exception, defaulting to another string on an empty message
	 *
	 * @param e   The exception to wrap
	 * @param def The default message
	 * @return The created exception
	 */
	public static LuaException rewriteException(Throwable e, String def) {
		String message = e.getMessage();
		return new LuaException((message == null || message.isEmpty()) ? def : message);
	}

	/**
	 * Wraps an exception, including its type
	 *
	 * @param e The exception to wrap
	 * @return The created exception
	 */
	public static LuaException rewriteWholeException(Throwable e) {
		return e instanceof LuaException ? (LuaException) e : new LuaException(e.toString());
	}


	public static ILuaMachine createMachine(Computer computer) {
		if (Config.Computer.cobalt) {
			return new CobaltMachine(computer);
		} else if (Config.Computer.rembulan) {
			return new RembulanMachine(computer);
		} else {
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
	}

	private static final Pattern INVALID_PATTERN = Pattern.compile("[^ -~]");

	public static String limitLabel(String label) {
		if (label == null) return null;

		if (Config.Computer.limitedLabels) {
			label = INVALID_PATTERN.matcher(label).replaceAll("");
		}

		if (label.length() > 32) {
			label = label.substring(0, 32);
		}

		return label;
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
