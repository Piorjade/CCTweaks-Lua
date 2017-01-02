package org.squiddev.cctweaks.api.lua;

/**
 * The base interface for all CCTweaks-Lua plugins.
 *
 * This will be loaded through a {@link java.util.ServiceLoader}. Create a file called
 * "META-INF/services/org.squiddev.cctweaks.api.lua.CCTweaksPlugin" with the name of the class(es) to load.
 *
 * Note, this will not be loaded in a Minecraft environment: you should create a mod instead.
 */
public abstract class CCTweaksPlugin {
	/**
	 * Register this plugin within an environment
	 *
	 * @param environment The environment to register it in.
	 */
	public abstract void register(ILuaEnvironment environment);
}
