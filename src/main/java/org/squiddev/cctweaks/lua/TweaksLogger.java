package org.squiddev.cctweaks.lua;

import org.squiddev.patcher.Logger;

public class TweaksLogger {
	public static Logger instance = Logger.instance;

	public static void debug(String message) {
		instance.doDebug(message);
	}

	public static void warn(String message) {
		instance.doWarn(message);
	}

	public static void error(String message, Throwable e) {
		instance.doError(message, e);
	}
}
