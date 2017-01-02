package org.squiddev.cctweaks.lua.patch;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.core.lua.ILuaMachine;
import dan200.computercraft.core.terminal.Terminal;
import org.squiddev.cctweaks.lua.patch.iface.ComputerPatched;
import org.squiddev.cctweaks.lua.patch.iface.IComputerEnvironmentExtended;
import org.squiddev.patcher.visitors.MergeVisitor;

public class Computer_Patch extends Computer implements ComputerPatched {
	@MergeVisitor.Stub
	private State m_state;

	@MergeVisitor.Stub
	private ILuaMachine m_machine;

	@MergeVisitor.Stub
	private boolean m_startRequested;

	@MergeVisitor.Stub
	private final IComputerEnvironment m_environment = null;

	@MergeVisitor.Stub
	public Computer_Patch(IComputerEnvironment environment, Terminal terminal, int id) {
		super(environment, terminal, id);
	}

	/**
	 * Abort will always trigger if the computer is running.
	 *
	 * @param hard If a hard abort should be triggered.
	 */
	public void abort(boolean hard) {
		synchronized (this) {
			if (m_state != State.Off && m_machine != null) {
				if (hard) {
					m_machine.hardAbort("Too long without yielding");
				} else {
					m_machine.softAbort("Too long without yielding");
				}
			}

		}
	}

	/**
	 * Determine whether a computer is on or is starting
	 *
	 * @return If the computer is on
	 */
	public boolean isMostlyOn() {
		synchronized (this) {
			return m_startRequested || m_state != State.Off;
		}
	}

	/**
	 * If this computer should no longer handle events
	 *
	 * @return If this computer should not resume
	 * @see IComputerEnvironmentExtended#suspendEvents()
	 */
	public boolean suspendEvents() {
		return m_environment instanceof IComputerEnvironmentExtended && ((IComputerEnvironmentExtended) m_environment).suspendEvents();
	}

	@MergeVisitor.Stub
	private enum State {
		Off,
		Starting,
		Running,
		Stopping,
	}
}
