package org.squiddev.cctweaks.lua.patch.binfs;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.cctweaks.api.lua.IArguments;
import org.squiddev.cctweaks.api.lua.ILuaObjectWithArguments;
import org.squiddev.cctweaks.api.lua.IMethodDescriptor;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.io.IOException;

/**
 * Basic file objects
 */
@MergeVisitor.Rewrite
@MergeVisitor.Rename(
	from = "org/squiddev/cctweaks/lua/patch/binfs/INormalFile",
	to = "dan200/computercraft/core/filesystem/IMountedFileNormal"
)
public class ReaderObject implements ILuaObjectWithArguments, IMethodDescriptor {
	private final INormalFile stream;

	public ReaderObject(INormalFile stream) {
		this.stream = stream;
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"readLine", "readAll", "close"};
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] args) throws LuaException, InterruptedException {
		switch (method) {
			case 0:
				try {
					byte[] result = stream.readLine();
					if (result != null) return new Object[]{result};
					return null;
				} catch (IOException ignored) {
				}
				return null;
			case 1:
				try {
					byte[] result = stream.readAll();
					if (result != null) return new Object[]{result};
					return null;
				} catch (IOException ignored) {
				}
				return null;
			case 2:
				try {
					stream.close();
				} catch (IOException ignored) {
				}
				return null;
			default:
				return null;
		}
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, IArguments arguments) throws LuaException, InterruptedException {
		return callMethod(context, method, arguments.asBinary());
	}

	@Override
	public boolean willYield(int method) {
		return false;
	}
}
