package org.squiddev.cctweaks.lua.patch.iface;

import dan200.computercraft.core.filesystem.FileSystemException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Methods which are patched onto {@link dan200.computercraft.core.filesystem.FileSystem}. You can safely cast to this.
 */
public interface FileSystemPatched {
	InputStream openForReadStream(String path) throws FileSystemException;

	OutputStream openForWriteStream(String path) throws FileSystemException;
}
