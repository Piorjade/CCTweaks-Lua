package org.squiddev.cctweaks.lua;

/**
 * A trivial way of signalling
 */
public final class Semaphore {
	private volatile boolean state = false;

	public synchronized void signal() {
		state = true;
		notify();
	}

	public synchronized void await() throws InterruptedException {
		while (!state) wait();
		state = false;
	}

	public synchronized boolean await(long timeout) throws InterruptedException {
		if (!state) {
			wait(timeout);
			if (!state) return false;
		}
		state = false;
		return true;
	}
}
