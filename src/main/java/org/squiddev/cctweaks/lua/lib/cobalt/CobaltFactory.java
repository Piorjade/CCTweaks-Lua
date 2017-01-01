package org.squiddev.cctweaks.lua.lib.cobalt;

import dan200.computercraft.core.computer.Computer;
import org.squiddev.cctweaks.api.lua.ILuaMachineFactory;

public class CobaltFactory implements ILuaMachineFactory<CobaltMachine> {
	@Override
	public String getID() {
		return "cobalt";
	}

	@Override
	public CobaltMachine create(Computer computer) {
		return new CobaltMachine(computer);
	}

	@Override
	public boolean supportsMultithreading() {
		return true;
	}
}
