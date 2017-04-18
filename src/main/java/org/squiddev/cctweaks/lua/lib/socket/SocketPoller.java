package org.squiddev.cctweaks.lua.lib.socket;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.ThreadBuilder;
import org.squiddev.cctweaks.lua.TweaksLogger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;

public class SocketPoller implements Runnable {
	private static final ExecutorService threads = ThreadBuilder.createThread("Socket", Config.APIs.Socket.threads, ThreadBuilder.LOW_PRIORITY);
	private static final ThreadFactory pollerFactory = ThreadBuilder.getFactory("Socket Poller", ThreadBuilder.LOW_PRIORITY);
	private static final EventLoopGroup group = new NioEventLoopGroup(Config.APIs.Socket.nettyThreads, ThreadBuilder.getFactory("Netty", ThreadBuilder.LOW_PRIORITY));

	private static final Object lock = new Object();
	private static SocketPoller read;
	private static SocketPoller connect;

	private final int flag;
	private final ConcurrentLinkedQueue<SocketAction> channels = new ConcurrentLinkedQueue<SocketAction>();
	private final Selector selector;

	private static final class SocketAction {
		final SocketChannel channel;
		final Runnable runnable;

		private SocketAction(SocketChannel channel, Runnable runnable) {
			this.channel = channel;
			this.runnable = runnable;
		}
	}

	private SocketPoller(int flag) {
		this.flag = flag;

		Selector selector = null;
		try {
			selector = Selector.open();
		} catch (IOException e) {
			TweaksLogger.error("Cannot run SocketPoller: sockets will not work as expected", e);
		}
		this.selector = selector;
		if (selector == null) return;

		pollerFactory.newThread(this).start();
	}

	@Override
	public void run() {
		while (true) {
			try {
				// Add all new sockets
				while (true) {
					SocketAction action = channels.poll();
					if (action == null) break;

					SocketChannel channel = action.channel;
					if (channel != null && channel.isOpen()) {
						channel.register(selector, flag, action.runnable);
					}
				}

				// Wait for something
				selector.select();

				// Queue which ones we've had a changed state
				for (SelectionKey key : selector.selectedKeys()) {
					if ((key.readyOps() & flag) != 0) {
						key.cancel();
						((Runnable) key.attachment()).run();
					}
				}
			} catch (IOException e) {
				TweaksLogger.error("Error in SocketPoller: running another iteration", e);
			}
		}
	}

	public void add(SocketChannel channel, Runnable action) {
		channels.offer(new SocketAction(channel, action));
		if (selector != null) selector.wakeup();
	}

	public static SocketPoller getRead() {
		if (read == null) {
			synchronized (lock) {
				if (read == null) read = new SocketPoller(SelectionKey.OP_READ);
			}
		}
		return read;
	}

	public static SocketPoller getConnect() {
		if (connect == null) {
			synchronized (lock) {
				if (connect == null) connect = new SocketPoller(SelectionKey.OP_CONNECT);
			}
		}
		return connect;
	}

	public static <T> Future<T> submit(Callable<T> task) {
		return threads.submit(task);
	}

	public static EventLoopGroup group() {
		return group;
	}
}
