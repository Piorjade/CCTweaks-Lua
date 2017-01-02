package org.squiddev.cctweaks.lua.patch.iface;

import dan200.computercraft.core.filesystem.IMountedFileNormal;
import org.squiddev.cctweaks.lua.lib.BinaryConverter;

import java.io.IOException;

/**
 * Methods which are patched onto {@link dan200.computercraft.core.filesystem.IMountedFileNormal}.
 * You can safely cast to this.
 */
public abstract class MountedNormalFilePatched implements IMountedFileNormal {
	public abstract byte[] readLineByte() throws IOException;

	public abstract byte[] readAllByte() throws IOException;

	public abstract void write(byte[] data, int start, int length, boolean newLine) throws IOException;

	@Override
	public String readLine() throws IOException {
		return BinaryConverter.decodeString(readLineByte());
	}

	@Override
	public void write(String data, int start, int length, boolean newLine) throws IOException {
		write(BinaryConverter.toBytes(data), start, length, newLine);
	}
}
