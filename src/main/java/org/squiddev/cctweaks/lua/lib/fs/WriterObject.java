package org.squiddev.cctweaks.lua.lib.fs;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.filesystem.IMountedFile;
import org.squiddev.cctweaks.api.lua.IArguments;
import org.squiddev.cctweaks.api.lua.ILuaObjectWithArguments;
import org.squiddev.cctweaks.api.lua.IMethodDescriptor;
import org.squiddev.cctweaks.lua.lib.BinaryConverter;
import org.squiddev.cctweaks.lua.patch.iface.MountedNormalFilePatched;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Basic file objects
 */
public class WriterObject implements ILuaObjectWithArguments, IMethodDescriptor {
	private final MountedNormalFilePatched stream;

	public WriterObject(IMountedFile stream) {
		this.stream = (MountedNormalFilePatched) stream;
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"write", "writeLine", "close", "flush"};
	}

	private void write(Object[] args, boolean newLine) throws LuaException {
		byte[] result;
		if (args.length > 0 && args[0] != null) {
			result = args[0] instanceof byte[] ? (byte[]) args[0] : BinaryConverter.toBytes(args[0].toString());
		} else {
			result = new byte[0];
		}

		try {
			stream.write(result, 0, result.length, newLine);
		} catch (IOException e) {
			throw new LuaException(e.getMessage());
		}
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] args) throws LuaException {
		switch (method) {
			case 0: {
				write(args, false);
				return null;
			}
			case 1:
				write(args, true);
				return null;
			case 2:
				try {
					stream.close();
					return null;
				} catch (IOException ignored) {
					return null;
				}
			case 3:
				try {
					stream.flush();
					return null;
				} catch (IOException ignored) {
					return null;
				}
			default:
				return null;
		}
	}

	@Override
	public Object[] callMethod(@Nonnull ILuaContext context, int method, @Nonnull IArguments arguments) throws LuaException, InterruptedException {
		return callMethod(context, method, arguments.asBinary());
	}

	@Override
	public boolean willYield(int method) {
		return false;
	}
}
