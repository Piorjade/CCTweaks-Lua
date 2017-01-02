package org.squiddev.cctweaks.lua.lib;

import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.cctweaks.api.lua.CCTweaksPlugin;
import org.squiddev.cctweaks.api.lua.ILuaAPI;
import org.squiddev.cctweaks.api.lua.ILuaAPIFactory;
import org.squiddev.cctweaks.api.lua.ILuaEnvironment;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.lib.cobalt.CobaltFactory;
import org.squiddev.cctweaks.lua.lib.luaj.LuaJFactory;
import org.squiddev.cctweaks.lua.lib.socket.SocketAPI;

import java.util.ServiceLoader;

public class ApiRegister {
	public static void init() {
		ILuaEnvironment environment = LuaEnvironment.instance;

		environment.registerMachine(new LuaJFactory());
		environment.registerMachine(new CobaltFactory());

		environment.registerAPI(new ILuaAPIFactory() {
			@Override
			public ILuaAPI create(IComputerAccess computer) {
				return Config.APIs.Socket.enabled ? new SocketAPI(computer) : null;
			}

			@Override
			public String[] getNames() {
				return new String[]{"socket"};
			}
		});

		environment.registerAPI(new DataAPI());
	}

	public static void loadPlugins() {
		ILuaEnvironment environment = LuaEnvironment.instance;

		for (CCTweaksPlugin plugin : ServiceLoader.load(CCTweaksPlugin.class, ApiRegister.class.getClassLoader())) {
			plugin.register(environment);
		}
	}
}
