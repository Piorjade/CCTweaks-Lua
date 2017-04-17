package org.squiddev.cctweaks.lua.lib.socket;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.cctweaks.api.lua.ILuaAPI;
import org.squiddev.cctweaks.api.lua.IMethodDescriptor;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.lib.LuaHelpers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import static org.squiddev.cctweaks.lua.lib.ArgumentHelper.getString;
import static org.squiddev.cctweaks.lua.lib.ArgumentHelper.optInt;

public class SocketAPI implements ILuaAPI, IMethodDescriptor {
	protected final HashSet<AbstractSocketConnection> connections = new HashSet<AbstractSocketConnection>();
	private final IComputerAccess computer;
	private int id = 0;

	public SocketAPI(IComputerAccess computer) {
		this.computer = computer;
	}

	@Override
	public void startup() {
		id = 0;
	}

	@Override
	public void shutdown() {
		for (AbstractSocketConnection connection : connections) {
			connection.close(false);
		}

		connections.clear();
	}

	@Override
	public void advance(double timestep) {
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"connect"};
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
		switch (method) {
			case 0: {
				String address = getString(arguments, 0);
				int port = optInt(arguments, 1, -1);

				if (!Config.APIs.Socket.enabled) throw new LuaException("TCP connections are disabled");
				if (connections.size() >= Config.APIs.Socket.maxTcpConnections) {
					throw new LuaException("Too many open connections");
				}

				URI uri = checkUri(address, port);
				try {
					AbstractSocketConnection connection = new SocketConnection(this, computer, id++);
					connection.open(uri, port);
					connections.add(connection);
					return new Object[]{connection};
				} catch (IOException e) {
					throw LuaHelpers.rewriteException(e, "Connection error");
				}
			}
			default:
				return null;
		}
	}

	private URI checkUri(String address, int port) throws LuaException {
		try {
			URI parsed = new URI(address);
			if (parsed.getHost() != null && (parsed.getPort() > 0 || port > 0)) {
				return parsed;
			}
		} catch (URISyntaxException ignored) {
		}

		try {
			URI simple = new URI("oc://" + address);
			if (simple.getHost() != null) {
				if (simple.getPort() > 0) {
					return simple;
				} else if (port > 0) {
					return new URI(simple.toString() + ":" + port);
				}
			}
		} catch (URISyntaxException ignored) {
		}

		throw new LuaException("Address could not be parsed or no valid port given");
	}

	@Override
	public boolean willYield(int method) {
		return false;
	}
}
