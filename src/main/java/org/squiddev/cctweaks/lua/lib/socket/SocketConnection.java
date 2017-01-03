package org.squiddev.cctweaks.lua.lib.socket;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.lib.LuaHelpers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SocketConnection extends AbstractSocketConnection {
	public SocketConnection(SocketAPI owner, IComputerAccess computer, int id) throws IOException {
		super(owner, computer, id);
	}

	@Override
	protected int write(byte[] contents) throws LuaException, InterruptedException {
		if (checkConnected()) {
			try {
				return getChannel().write(ByteBuffer.wrap(contents));
			} catch (IOException e) {
				throw LuaHelpers.rewriteException(e, "Socket error");
			}
		} else {
			return 0;
		}
	}

	@Override
	protected byte[] read(int count) throws LuaException, InterruptedException {
		count = Math.min(count, Config.APIs.Socket.maxRead);

		if (checkConnected()) {
			ByteBuffer buffer = ByteBuffer.allocate(count);
			try {
				int read = getChannel().read(buffer);
				if (read == -1) return null;

				// Re-enqueue the listener
				addReadListener();
				return Arrays.copyOf(buffer.array(), read);
			} catch (IOException e) {
				throw LuaHelpers.rewriteException(e, "Socket error");
			}
		} else {
			return new byte[0];
		}
	}

	@Override
	protected InetSocketAddress connect(URI uri, int port) throws Exception {
		InetSocketAddress address = super.connect(uri, port);

		SocketPoller.getConnect().add(getChannel(), new Runnable() {
			@Override
			public void run() {
				try {
					getChannel().finishConnect();
				} catch (IOException ignored) {
				}
				onConnectFinished();
			}
		});
		return address;
	}
}
