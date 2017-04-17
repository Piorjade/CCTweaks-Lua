package org.squiddev.cctweaks.lua.lib.socket;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.cctweaks.api.lua.IArguments;
import org.squiddev.cctweaks.api.lua.ILuaObjectWithArguments;
import org.squiddev.cctweaks.api.lua.IMethodDescriptor;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.lib.BinaryConverter;
import org.squiddev.cctweaks.lua.lib.LuaHelpers;
import org.squiddev.patcher.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class AbstractSocketConnection implements ILuaObjectWithArguments, IMethodDescriptor, ISocketListener {
	private final SocketAPI owner;
	private final IComputerAccess computer;
	private final int id;

	private Future<Object> address;
	private SocketChannel channel;
	private boolean isResolved = false;

	private final Runnable readCallback = new Runnable() {
		@Override
		public void run() {
			onMessage();
		}
	};

	public AbstractSocketConnection(SocketAPI owner, IComputerAccess computer, int id) throws IOException {
		this.owner = owner;
		this.computer = computer;
		this.id = id;

		channel = SocketChannel.open();
		channel.configureBlocking(false);
	}

	public void open(final URI uri, final int port) throws IOException {
		address = SocketPoller.submit(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				connect(uri, port);
				return null;
			}
		});
	}

	public void close(boolean remove) {
		if (remove) owner.connections.remove(this);

		if (address != null) {
			address.cancel(true);
			address = null;
		}

		if (channel != null) {
			try {
				channel.close();
			} catch (IOException e) {
				Logger.error("Cannot close socket", e);
			}
			channel = null;
		}
	}

	protected boolean checkConnected() throws LuaException, InterruptedException {
		if (channel == null || address == null) throw new LuaException("Socket is closed");
		try {
			if (isResolved) return channel.finishConnect();

			if (address.isCancelled()) {
				close(true);
				throw new LuaException("Bad connection descriptor");
			}

			if (address.isDone()) {
				try {
					address.get();
				} catch (ExecutionException e) {
					Throwable cause = e.getCause();
					if (cause instanceof UnknownHostException) {
						throw new LuaException("Cannot resolve host " + cause.getMessage());
					} else {
						throw LuaHelpers.rewriteException(cause != null ? cause : e, "Socket error");
					}
				}
				isResolved = true;
				return channel.finishConnect();
			}

			return false;
		} catch (IOException e) {
			throw LuaHelpers.rewriteException(e, "Socket error");
		}
	}

	protected SocketChannel getChannel() {
		return channel;
	}

	protected InetSocketAddress connect(URI uri, int port) throws Exception {
		InetAddress resolved = InetAddress.getByName(uri.getHost());
		if (Config.APIs.Socket.blacklist.active && Config.APIs.Socket.blacklist.matches(resolved, uri.getHost())) {
			throw new LuaException("Address is blacklisted");
		}

		if (Config.APIs.Socket.whitelist.active && !Config.APIs.Socket.whitelist.matches(resolved, uri.getHost())) {
			throw new LuaException("Address is not whitelisted");
		}

		InetSocketAddress address = new InetSocketAddress(resolved, uri.getPort() == -1 ? port : uri.getPort());
		channel.connect(address);

		return address;
	}

	protected abstract int write(byte[] contents) throws LuaException, InterruptedException;

	protected abstract byte[] read(int count) throws LuaException, InterruptedException;

	@Override
	public String[] getMethodNames() {
		return new String[]{"checkConnected", "close", "read", "write", "id"};
	}

	@Override
	public Object[] callMethod(@Nonnull ILuaContext context, int method, @Nonnull IArguments arguments) throws LuaException, InterruptedException {
		switch (method) {
			case 0:
				return new Object[]{checkConnected()};
			case 1:
				if (address == null && channel == null) throw new LuaException("Socket already closed");
				close(true);
				return null;
			case 2: {
				int count = Integer.MAX_VALUE;
				Object argument = arguments.getArgument(0);
				if (argument instanceof Number) {
					count = Math.max(0, ((Number) argument).intValue());
				} else if (argument != null) {
					throw new LuaException("Expected number");
				}

				byte[] contents = read(count);
				return new Object[]{contents};
			}
			case 3: {
				int written = write(arguments.getStringBytes(0));
				return new Object[]{written};
			}
			case 4:
				return new Object[]{id};
			default:
				return null;
		}
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
		switch (method) {
			case 0:
				return new Object[]{checkConnected()};
			case 1:
				if (address == null || channel == null) throw new LuaException("Socket already closed");
				close(true);
				return null;
			case 2: {
				int count = Integer.MAX_VALUE;
				if (arguments.length >= 1 && arguments[0] != null) {
					if (arguments[0] instanceof Number) {
						count = Math.max(0, ((Number) arguments[0]).intValue());
					} else {
						throw new LuaException("Expected number");
					}
				}

				byte[] contents = read(count);
				return new Object[]{contents};
			}
			case 3: {
				byte[] stream;
				if (arguments.length == 0) throw new LuaException("Expected string");

				Object argument = arguments[0];
				if (argument instanceof byte[]) {
					stream = (byte[]) argument;
				} else if (argument instanceof String) {
					stream = BinaryConverter.toBytes((String) argument);
				} else {
					throw new LuaException("Expected string");
				}

				int written = write(stream);
				return new Object[]{written};
			}
			case 4:
				return new Object[]{id};
			default:
				return null;
		}
	}

	@Override
	public boolean willYield(int method) {
		return false;
	}

	@Override
	public void onClosed() {
		close(true);
		computer.queueEvent("socket_closed", new Object[]{id});
	}

	@Override
	public void onConnectFinished() {
		computer.queueEvent("socket_connect", new Object[]{id});
		addReadListener();
	}

	@Override
	public void onMessage() {
		computer.queueEvent("socket_message", new Object[]{id});
	}

	protected void addReadListener() {
		SocketPoller.getRead().add(getChannel(), readCallback);
	}
}
