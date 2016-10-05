package org.squiddev.cctweaks.lua.lib;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.IComputerEnvironment;
import org.squiddev.patcher.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LuaMachineHelpers {
	public static final String[] ILLEGAL_NAMES = new String[]{
		"collectgarbage",
		"dofile",
		"loadfile",
		"print"
	};

	private static final Method getHost;

	static {
		Method host = null;
		try {
			host = IComputerEnvironment.class.getMethod("getHostString");
		} catch (NoSuchMethodException ignored) {
		} catch (RuntimeException e) {
			Logger.error("Unknown error getting host string", e);
		}

		getHost = host;
	}

	public static String getHost(Computer computer) {
		if (computer == null) return null;

		try {
			// We have to use reflection for different CC versions
			return (String) getHost.invoke(computer.getAPIEnvironment().getComputerEnvironment());
		} catch (InvocationTargetException e) {
			Logger.error("Cannot find getHostString", e);
		} catch (IllegalAccessException e) {
			Logger.error("Cannot find getHostString", e);
		} catch (RuntimeException e) {
			Logger.error("Unknown error with setting _HOST", e);
		}

		return null;
	}
}
