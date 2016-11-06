package org.squiddev.cctweaks.lua.lib;

import dan200.computercraft.core.computer.Computer;
import org.squiddev.cctweaks.lua.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ComputerMonitor {
	private static ComputerMonitor instance;
	private static final Object lock = new Object();

	private final HashMap<Computer, ComputerEntry> entries = new HashMap<Computer, ComputerEntry>();

	public static void start() {
		if (!Config.Computer.MultiThreading.enabled) {
			throw new IllegalStateException("Cannot monitor without Multithreading enabled");
		}

		synchronized (lock) {
			if (instance != null) throw new IllegalStateException("Already monitoring");

			instance = new ComputerMonitor();
		}
	}

	public static ComputerMonitor stop() {
		synchronized (lock) {
			ComputerMonitor monitor = instance;
			if (monitor == null) throw new IllegalStateException("Not monitoring");

			instance = null;
			return monitor;
		}
	}

	public static ComputerMonitor get() {
		return instance;
	}

	public synchronized void increment(Computer computer, long time) {
		ComputerEntry entry = entries.get(computer);
		if (entry == null) {
			entry = new ComputerEntry(computer);
			entries.put(computer, entry);
		}

		entry.increment(time);
	}

	public synchronized List<ComputerEntry> getEntries() {
		return Collections.unmodifiableList(new ArrayList<ComputerEntry>(entries.values()));
	}

	public static class ComputerEntry {
		private final Computer computer;

		private long time;
		private int tasks;

		public ComputerEntry(Computer computer) {
			this.computer = computer;
		}

		public void increment(long time) {
			this.time += time;
			this.tasks++;
		}

		public Computer getComputer() {
			return computer;
		}

		public long getTime() {
			return time;
		}

		public int getTasks() {
			return tasks;
		}
	}
}
