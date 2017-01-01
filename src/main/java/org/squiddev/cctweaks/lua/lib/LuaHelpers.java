package org.squiddev.cctweaks.lua.lib;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import org.luaj.vm2.Varargs;
import org.objectweb.asm.ClassVisitor;
import org.squiddev.cctweaks.api.lua.ArgumentDelegator;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.lib.luaj.LuaJArguments;

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
}
