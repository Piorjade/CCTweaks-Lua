package org.squiddev.cctweaks.lua.patch;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.ComputerThread;
import dan200.computercraft.core.computer.ITask;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.ThreadBuilder;
import org.squiddev.cctweaks.lua.lib.ComputerMonitor;
import org.squiddev.patcher.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.*;

/**
 * Rewrite of {@link ComputerThread} supporting multiple threads.
 */
public class ComputerThread_Rewrite {
	private static final int QUEUE_LIMIT = 256;

	/**
	 * Lock used for modifications to the object
	 */
	private static final Object stateLock = new Object();

	/**
	 * Lock for various task operations
	 */
	private static final Object taskLock = new Object();

	/**
	 * Map of objects to task list
	 */
	private static final WeakHashMap<Object, BlockingQueue<ITask>> computerTaskQueues = new WeakHashMap<Object, BlockingQueue<ITask>>();

	/**
	 * Active queues to execute
	 */
	private static final BlockingQueue<BlockingQueue<ITask>> computerTasksActive = new LinkedBlockingQueue<BlockingQueue<ITask>>();
	private static final Set<BlockingQueue<ITask>> computerTasksActiveSet = new HashSet<BlockingQueue<ITask>>();

	/**
	 * The default object for items which don't have an owner
	 */
	private static final Object defaultOwner = new Object();

	/**
	 * Whether the thread is stopped or should be stopped
	 */
	private static boolean stopped = false;

	/**
	 * The thread tasks execute on
	 */
	private static Thread[] threads = null;

	private static final ThreadFactory mainFactory = ThreadBuilder.getFactory("Computer-Tasks", Config.Computer.MultiThreading.priority);
	private static final ThreadFactory delegateFacotry = ThreadBuilder.getFactory("Computer-Delegate", Config.Computer.MultiThreading.priority);

	/**
	 * Start the computer thread
	 */
	public static void start() {
		synchronized (stateLock) {
			stopped = false;
			if (threads == null) threads = new Thread[Config.Computer.MultiThreading.threads];

			for (int i = 0; i < threads.length; i++) {
				Thread thread = threads[i];
				if (thread == null || !thread.isAlive()) {
					thread = threads[i] = mainFactory.newThread(new TaskExecutor());
					thread.start();
				}
			}
		}
	}

	/**
	 * Attempt to stop the computer thread
	 */
	public static void stop() {
		synchronized (stateLock) {
			if (threads != null) {
				stopped = true;
				for (Thread thread : threads) {
					if (thread != null && thread.isAlive()) {
						thread.interrupt();
					}
				}
			}
		}
	}

	/**
	 * Queue a task to execute on the thread
	 *
	 * @param task     The task to execute
	 * @param computer The computer to execute it on, use {@code null} to execute on the default object.
	 */
	public static void queueTask(ITask task, Computer computer) {
		Object queueObject = computer == null ? defaultOwner : computer;

		BlockingQueue<ITask> queue = computerTaskQueues.get(queueObject);
		if (queue == null) {
			computerTaskQueues.put(queueObject, queue = new LinkedBlockingQueue<ITask>(QUEUE_LIMIT));
		}

		synchronized (taskLock) {
			if (queue.offer(task) && !computerTasksActiveSet.contains(queue)) {
				computerTasksActive.add(queue);
				computerTasksActiveSet.add(queue);
			}
		}
	}

	private static final class TaskExecutor implements Runnable {
		private final ExecutorService executor = Executors.newSingleThreadExecutor(delegateFacotry);

		@Override
		public void run() {
			try {
				while (true) {
					// Wait for an active queue to execute
					BlockingQueue<ITask> queue = computerTasksActive.take();

					// If threads should be stopped then return
					synchronized (stateLock) {
						if (stopped) return;
					}

					execute(queue);
				}
			} catch (InterruptedException ignored) {
			}
		}

		private void execute(BlockingQueue<ITask> queue) {
			try {
				final ITask task = queue.take();

				// Execute the task
				Future<?> worker = executor.submit(new Runnable() {
					public void run() {
						try {
							task.execute();
						} catch (Throwable e) {
							Logger.error("ComputerCraft: Error running task.", e);
						}
					}
				});

				// Execute the task
				long start = System.currentTimeMillis();
				try {
					worker.get(Config.Computer.computerThreadTimeout, TimeUnit.MILLISECONDS);
				} catch (TimeoutException ignored) {
				}

				// If we timed out rather than exiting:
				if (!worker.isDone()) {
					// Attempt to soft then hard abort
					Computer computer = task.getOwner();
					if (computer != null) {
						computer.abort(false);
						try {
							worker.get(1500, TimeUnit.MILLISECONDS);
						} catch (TimeoutException ignored) {
						}

						if (!worker.isDone()) {
							computer.abort(true);
							try {
								worker.get(1500, TimeUnit.MILLISECONDS);
							} catch (TimeoutException ignored) {
							}
						}
					}

					// Interrupt the thread
					if (!worker.isDone()) {
						worker.cancel(true);
					}
				}

				long end = System.currentTimeMillis();
				Computer owner = task.getOwner();
				if (owner != null) {
					ComputerMonitor monitor = ComputerMonitor.get();
					if (monitor != null) monitor.increment(owner, end - start);
				}
			} catch (InterruptedException ignored) {
			} catch (ExecutionException e) {
				Logger.error("ComputerCraft: Error running task.", e);
			}

			// Re-add it back onto the queue or remove it
			synchronized (taskLock) {
				if (queue.isEmpty()) {
					computerTasksActiveSet.remove(queue);
				} else {
					computerTasksActive.add(queue);
				}
			}
		}
	}
}
