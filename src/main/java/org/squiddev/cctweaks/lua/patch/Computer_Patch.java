package org.squiddev.cctweaks.lua.patch;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.core.lua.ILuaMachine;
import dan200.computercraft.core.terminal.Terminal;
import org.squiddev.patcher.visitors.MergeVisitor;

public class Computer_Patch extends Computer {
	@MergeVisitor.Stub
	private State m_state;

	@MergeVisitor.Stub
	private ILuaMachine m_machine;

	@MergeVisitor.Stub
	public Computer_Patch(IComputerEnvironment environment, Terminal terminal, int id) {
		super(environment, terminal, id);
	}

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

	@MergeVisitor.Stub
	private static enum State {
		Off,
		Starting,
		Running,
		Stopping;

		private State() {
		}
	}
}
