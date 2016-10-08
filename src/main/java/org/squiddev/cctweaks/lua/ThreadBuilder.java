package org.squiddev.cctweaks.lua;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper methods for various things
 */
public class ThreadBuilder {
	public static int THREAD_PRIORITY = Thread.MIN_PRIORITY + (Thread.NORM_PRIORITY - Thread.MIN_PRIORITY) / 2;

	public static ThreadPoolExecutor createThread(String name, int threads) {
		final String prefix = "CCTweaks-" + name + "-";
		final AtomicInteger counter = new AtomicInteger(1);

		SecurityManager manager = System.getSecurityManager();
		final ThreadGroup group = manager == null ? Thread.currentThread().getThreadGroup() : manager.getThreadGroup();
		return new ThreadPoolExecutor(
			threads, Integer.MAX_VALUE,
			60L, TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>(),
			new ThreadFactory() {
				@Override
				public Thread newThread(Runnable runnable) {
					Thread thread = new Thread(group, runnable, prefix + counter.getAndIncrement());
					if (!thread.isDaemon()) thread.setDaemon(true);
					if (thread.getPriority() != THREAD_PRIORITY) thread.setPriority(THREAD_PRIORITY);

					return thread;
				}
			}
		);
	}
}
