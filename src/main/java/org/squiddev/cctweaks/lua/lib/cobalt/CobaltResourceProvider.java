package org.squiddev.cctweaks.lua.lib.cobalt;

import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;
import org.squiddev.cobalt.lib.platform.AbstractResourceManipulator;
import org.squiddev.cobalt.lib.profiler.ProfilerLib;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by JonathanCoates on 06/12/2016.
 */
public class CobaltResourceProvider extends AbstractResourceManipulator implements ProfilerLib.OutputProvider {
	private final Computer computer;
	private FileSystem fileSystem;
	private IWritableMount rootMount;

	public CobaltResourceProvider(Computer computer) {
		this.computer = computer;
	}

	private FileSystem getFileSystem() {
		if (fileSystem != null) return fileSystem;
		return fileSystem = computer.getAPIEnvironment().getFileSystem();
	}

	private IWritableMount getRootMount() {
		if (rootMount != null) return rootMount;
		return rootMount = computer.getRootMount();
	}

	@Override
	public InputStream findResource(String s) {
		try {
			return getRootMount().openForRead(s);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void rename(String from, String to) throws IOException {
		try {
			getFileSystem().move(from, to);
		} catch (FileSystemException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public void remove(String file) throws IOException {
		try {
			getFileSystem().delete(file);
		} catch (FileSystemException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public DataOutputStream createWriter(String name) throws IOException {
		return new DataOutputStream(getRootMount().openForWrite(name));
	}
}
