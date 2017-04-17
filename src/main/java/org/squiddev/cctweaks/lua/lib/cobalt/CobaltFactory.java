package org.squiddev.cctweaks.lua.lib.cobalt;

import dan200.computercraft.core.computer.Computer;
import org.squiddev.cctweaks.api.lua.ILuaMachineFactory;

import javax.annotation.Nonnull;

public class CobaltFactory implements ILuaMachineFactory<CobaltMachine> {
	@Nonnull
	@Override
	public String getID() {
		return "cobalt";
	}

	@Nonnull
	@Override
	public CobaltMachine create(Computer computer) {
		return new CobaltMachine(computer);
	}

	@Override
	public boolean supportsMultithreading() {
		return true;
	}

	@Nonnull
	@Override
	public String getPreBios() {
		return PRE_BIOS;
	}
}
