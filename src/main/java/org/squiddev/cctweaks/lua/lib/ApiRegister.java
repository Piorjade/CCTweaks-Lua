package org.squiddev.cctweaks.lua.lib;

import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.cctweaks.api.lua.ILuaAPI;
import org.squiddev.cctweaks.api.lua.ILuaAPIFactory;
import org.squiddev.cctweaks.api.lua.ILuaEnvironment;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.lib.socket.SocketAPI;

public class ApiRegister {
	public void init() {
		ILuaEnvironment environment = LuaEnvironment.instance;
		environment.registerAPI(new ILuaAPIFactory() {
			@Override
			public ILuaAPI create(IComputerAccess computer) {
				return Config.APIs.Socket.enabled ? new SocketAPI() : null;
			}

			@Override
			public String[] getNames() {
				return new String[]{"socket"};
			}
		});

		environment.registerAPI(new DataAPI());
	}
}
