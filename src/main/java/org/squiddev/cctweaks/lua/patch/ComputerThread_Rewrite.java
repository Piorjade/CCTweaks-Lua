package org.squiddev.cctweaks.lua.patch;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.ComputerThread;
import dan200.computercraft.core.computer.ITask;
import org.squiddev.cctweaks.api.lua.ILuaMachineFactory;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.Semaphore;
import org.squiddev.cctweaks.lua.ThreadBuilder;
import org.squiddev.cctweaks.lua.lib.ComputerMonitor;
import org.squiddev.cctweaks.lua.lib.LuaEnvironment;
import org.squiddev.patcher.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

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
	private static final ThreadFactory delegateFactory = ThreadBuilder.getFactory("Computer-Delegate", Config.Computer.MultiThreading.priority);

	/**
	 * Start the computer thread
	 */
	public static void start() {
		synchronized (stateLock) {
			ILuaMachineFactory<?> factory = LuaEnvironment.getUsedMachine();
			if (!factory.supportsMultithreading() && Config.Computer.MultiThreading.threads > 1) {
				Logger.warn("Can only have 1 thread when running on " + factory.getID() + " runtime, reverting to default");
				Config.Computer.MultiThreading.threads = 1;
			}

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
			if (queue.offer(task) && !computerTasksActiveSet.contains(queue) && !shouldSuspend(computer)) {
				computerTasksActive.add(queue);
				computerTasksActiveSet.add(queue);
			}
		}
	}

	/**
	 * Resume a computer which has been suspended
	 *
	 * @param computer The computer to resume
	 */
	public static void resumeComputer(Computer computer) {
		BlockingQueue<ITask> queue = computerTaskQueues.get(computer);
		if (queue == null) return;

		synchronized (taskLock) {
			if (!queue.isEmpty() && !computerTasksActiveSet.contains(queue) && !shouldSuspend(computer)) {
				computerTasksActive.add(queue);
				computerTasksActiveSet.add(queue);
			}
		}
	}

	private static boolean shouldSuspend(Computer computer) {
		return computer instanceof IComputerPatched && ((IComputerPatched) computer).suspendEvents();
	}

	private static final class TaskExecutor implements Runnable {
		private TaskRunner runner;
		private Thread thread;

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
			ITask task;
			try {
				task = queue.take();
			} catch (InterruptedException ignored) {
				return;
			}

			if (thread == null || !thread.isAlive()) {
				runner = new TaskRunner();
				thread = delegateFactory.newThread(runner);
				thread.start();
			}

			// Execute the task
			long start = System.currentTimeMillis();
			runner.submit(task);

			try {
				// If we timed out rather than exiting:
				boolean done = runner.await(Config.Computer.computerThreadTimeout);
				if (!done) {
					// Attempt to soft then hard abort
					Computer computer = task.getOwner();
					if (computer != null) {
						computer.abort(false);

						done = runner.await(1500);
						if (!done) {
							computer.abort(true);
							done = runner.await(1500);
						}
					}

					// Interrupt the thread
					if (!done) {
						thread.interrupt();
						thread = null;
						runner = null;
					}
				}
			} catch (InterruptedException ignored) {
			}

			long end = System.currentTimeMillis();
			Computer owner = task.getOwner();
			if (owner != null) {
				ComputerMonitor monitor = ComputerMonitor.get();
				if (monitor != null) monitor.increment(owner, end - start);
			}

			// Re-add it back onto the queue or remove it
			synchronized (taskLock) {
				if (queue.isEmpty() || shouldSuspend(owner)) {
					computerTasksActiveSet.remove(queue);
				} else {
					computerTasksActive.add(queue);
				}
			}
		}
	}

	private static final class TaskRunner implements Runnable {
		private final Semaphore input = new Semaphore();
		private final Semaphore finished = new Semaphore();
		private ITask task;

		@Override
		public void run() {
			try {
				while (true) {
					input.await();
					try {
						task.execute();
					} catch (Throwable e) {
						Logger.error("ComputerCraft: Error running task.", e);
					}
					task = null;
					finished.signal();
				}
			} catch (InterruptedException e) {
				Logger.error("ComputerCraft: Error running thread.", e);
			}
		}

		public void submit(ITask task) {
			this.task = task;
			input.signal();
		}

		public boolean await(long timeout) throws InterruptedException {
			return finished.await(timeout);
		}
	}
}
