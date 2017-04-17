package org.squiddev.cctweaks.lua.patch;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.OSAPI;
import org.squiddev.cctweaks.api.lua.IArguments;
import org.squiddev.cctweaks.api.lua.ILuaObjectWithArguments;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nonnull;

/**
 * Allows queueing events with binary data.
 */
public class OSAPI_Patch extends OSAPI implements ILuaObjectWithArguments {
	@MergeVisitor.Stub
	public OSAPI_Patch(IAPIEnvironment environment) {
		super(environment);
	}

	@Override
	public Object[] callMethod(@Nonnull ILuaContext context, int method, @Nonnull IArguments arguments) throws LuaException, InterruptedException {
		switch (method) {
			case 0:
				queueLuaEvent(arguments.getString(0), arguments.subArgs(1).asBinary());
				return null;
			default:
				return callMethod(context, method, arguments.asArguments());
		}
	}

	@MergeVisitor.Stub
	private void queueLuaEvent(String event, Object[] args) {
	}
}
