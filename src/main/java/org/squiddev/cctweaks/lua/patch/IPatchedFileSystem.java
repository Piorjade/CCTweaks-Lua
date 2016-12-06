package org.squiddev.cctweaks.lua.patch;

import dan200.computercraft.core.filesystem.FileSystemException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Allows access to FileSystem functions. Some exist already, some are manually patched on.
 */
public interface IPatchedFileSystem {
	InputStream openForReadStream(String path) throws FileSystemException;

	OutputStream openForWriteStream(String path) throws FileSystemException;

	void move(String from, String to) throws FileSystemException;

	void delete(String path) throws FileSystemException;
}
