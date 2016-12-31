package org.squiddev.cctweaks.lua.lib.socket;

import org.squiddev.cctweaks.lua.ThreadBuilder;
import org.squiddev.patcher.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketPoller implements Runnable {
	private static SocketPoller poller;

	private static final Object lock = new Object();
	private final ConcurrentLinkedQueue<SocketConnection> channels = new ConcurrentLinkedQueue<SocketConnection>();
	private Selector selector;

	public SocketPoller() {
		try {
			selector = Selector.open();
		} catch (IOException e) {
			Logger.error("Cannot run SocketPoller: sockets will not work as expected", e);
			return;
		}

		ThreadBuilder
			.getFactory("Socket Poller", ThreadBuilder.LOW_PRIORITY)
			.newThread(this)
			.start();
	}

	@Override
	public void run() {
		while (true) {
			try {
				// Add all new sockets
				while (true) {
					SocketConnection connection = channels.poll();
					if (connection == null) break;

					SocketChannel channel = connection.getChannel();
					if (channel != null && channel.isOpen()) {
						channel.register(selector, SelectionKey.OP_READ, connection);
					}
				}

				// Wait for a message
				selector.select();

				// Queue which ones we've received a message for
				for (SelectionKey key : selector.selectedKeys()) {
					if (key.isReadable()) {
						key.cancel();
						((SocketConnection) key.attachment()).onMessage();
					}
				}
			} catch (IOException e) {
				Logger.error("Error in SocketPoller: running another iteration", e);
			}
		}
	}

	public void add(SocketConnection connection) {
		channels.offer(connection);
		if (selector != null) selector.wakeup();
	}

	public static SocketPoller get() {
		if (poller == null) {
			synchronized (lock) {
				if (poller == null) poller = new SocketPoller();
			}
		}
		return poller;
	}
}
