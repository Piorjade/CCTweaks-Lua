package org.squiddev.cctweaks.lua.lib.socket;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.cctweaks.lua.lib.LuaHelpers;
import org.squiddev.patcher.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.SocketChannel;

public abstract class AbstractSocketConnection extends AbstractConnection {
	private SocketChannel channel;

	private final Runnable readCallback = new Runnable() {
		@Override
		public void run() {
			onMessage();
		}
	};

	public AbstractSocketConnection(SocketAPI owner, IComputerAccess computer, int id) throws IOException {
		super(owner, computer, id);

		channel = SocketChannel.open();
		channel.configureBlocking(false);
	}

	public void close(boolean remove) {
		super.close(remove);

		if (channel != null) {
			try {
				channel.close();
			} catch (IOException e) {
				Logger.error("Cannot close socket", e);
			}
			channel = null;
		}
	}

	@Override
	protected boolean isClosed() {
		return super.isClosed() || channel == null;
	}

	protected boolean checkConnected() throws LuaException, InterruptedException {
		try {
			return super.checkConnected() && channel.finishConnect();
		} catch (IOException e) {
			throw LuaHelpers.rewriteException(e, "Socket error");
		}
	}

	protected SocketChannel getChannel() {
		return channel;
	}

	protected InetSocketAddress connect(URI uri, int port) throws Exception {
		InetSocketAddress address = super.connect(uri, port);
		channel.connect(address);
		return address;
	}

	protected abstract int write(byte[] contents) throws LuaException, InterruptedException;

	protected abstract byte[] read(int count) throws LuaException, InterruptedException;

	@Override
	public void onConnectFinished() {
		super.onConnectFinished();
		addReadListener();
	}

	protected void addReadListener() {
		SocketPoller.getRead().add(getChannel(), readCallback);
	}
}
