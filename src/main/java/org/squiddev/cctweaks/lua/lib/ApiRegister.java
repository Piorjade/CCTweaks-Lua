package org.squiddev.cctweaks.lua.lib;

import org.squiddev.cctweaks.api.lua.*;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.lib.cobalt.CobaltFactory;
import org.squiddev.cctweaks.lua.lib.luaj.LuaJFactory;
import org.squiddev.cctweaks.lua.lib.socket.SocketAPI;

import javax.annotation.Nonnull;
import java.util.ServiceLoader;

public class ApiRegister {
	public static void init() {
		ILuaEnvironment environment = LuaEnvironment.instance;

		environment.registerMachine(new LuaJFactory());
		environment.registerMachine(new CobaltFactory());

		environment.registerAPI(new ILuaAPIFactory() {
			@Override
			public ILuaAPI create(@Nonnull IExtendedComputerAccess computer) {
				if (Config.APIs.Socket.tcp || Config.APIs.Socket.websocket) {
					return new SocketAPI(computer);
				} else {
					return null;
				}
			}

			@Nonnull
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
