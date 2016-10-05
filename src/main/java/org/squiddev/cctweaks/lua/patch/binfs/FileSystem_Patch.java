package org.squiddev.cctweaks.lua.patch.binfs;

import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;
import dan200.computercraft.core.filesystem.IMountedFile;
import dan200.computercraft.core.filesystem.IMountedFileBinary;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.io.*;
import java.util.Set;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "SynchronizeOnNonFinalField"})
@MergeVisitor.Rename(
	from = {
		"org/squiddev/cctweaks/lua/patch/binfs/INormalFile",
		"org/squiddev/cctweaks/lua/patch/binfs/LuaExceptionStub"
	},
	to = {
		"dan200/computercraft/core/filesystem/IMountedFileNormal",
		"dan200/computercraft/api/lua/LuaException"
	}
)
public class FileSystem_Patch extends FileSystem {
	@MergeVisitor.Stub
	private Set<IMountedFile> m_openFiles;

	private int openFilesCount;

	public FileSystem_Patch(String rootLabel, IWritableMount rootMount) throws FileSystemException {
		super(rootLabel, rootMount);
	}

	@MergeVisitor.Stub
	private static String sanitizePath(String path) {
		return path;
	}

	@MergeVisitor.Stub
	private MountWrapper getMount(String path) throws FileSystemException {
		return new MountWrapper();
	}

	@MergeVisitor.Stub
	private class MountWrapper {
		public InputStream openForRead(String path) throws FileSystemException {
			return null;
		}

		public OutputStream openForWrite(String path) throws FileSystemException {
			return null;
		}

		public OutputStream openForAppend(String path) throws FileSystemException {
			return null;
		}
	}

	public void addFile(IMountedFile file) {
		synchronized (m_openFiles) {
			m_openFiles.add(file);
			if (++openFilesCount > Config.Computer.maxFilesHandles) {
				// Ensure that we aren't over the open file limit
				// We throw Lua exceptions as FileSystemExceptions won't be handled by fs.open
				try {
					file.close();
				} catch (IOException e) {
					throw new LuaExceptionStub("Too many file handles: " + e.getMessage());
				}
				throw new LuaExceptionStub("Too many file handles");
			}
		}
	}

	public void removeFile(IMountedFile file, Closeable stream) throws IOException {
		synchronized (m_openFiles) {
			m_openFiles.remove(file);
			openFilesCount--;

			stream.close();
		}
	}

	@MergeVisitor.Rename(to = "openForRead")
	public synchronized INormalFile openForRead_P(String path) throws FileSystemException {
		path = sanitizePath(path);
		MountWrapper mount = getMount(path);
		InputStream stream = mount.openForRead(path);
		if (stream != null) {
			final BufferedInputStream reader = new BufferedInputStream(stream);
			INormalFile file = new INormalFile() {
				@MergeVisitor.Rewrite
				protected boolean ANNOTATION;

				@Override
				public byte[] readLine() throws IOException {
					// FIXME: Is this the most efficient way?
					ByteArrayOutputStream buffer = new ByteArrayOutputStream(128);
					int val;
					while ((val = reader.read()) != -1) {
						if (val == '\r') {
							// Peek one character ahead
							reader.mark(1);
							int newVal = reader.read();
							reader.reset();

							// Consume '\n' as well
							if (newVal == '\n') reader.read();
							return buffer.toByteArray();
						} else if (val == '\n') {
							return buffer.toByteArray();
						} else {
							buffer.write(val);
						}
					}

					// We never hit a new line, so we've reached the end of the stream
					return buffer.size() > 0 ? buffer.toByteArray() : null;
				}

				@Override
				public byte[] readAll() throws IOException {
					ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
					int nRead;
					byte[] data = new byte[1024];
					while ((nRead = reader.read(data, 0, data.length)) != -1) {
						buffer.write(data, 0, nRead);
					}

					return buffer.toByteArray();
				}

				@Override
				public void write(byte[] data, int start, int length, boolean newLine) throws IOException {
					throw new UnsupportedOperationException();
				}

				@Override
				public void close() throws IOException {
					removeFile(this, reader);
				}

				@Override
				public void flush() throws IOException {
					throw new UnsupportedOperationException();
				}
			};
			addFile(file);
			return file;
		}
		return null;
	}

	@MergeVisitor.Rename(to = "openForWrite")
	public synchronized INormalFile openForWrite_P(String path, boolean append) throws FileSystemException {
		path = sanitizePath(path);
		MountWrapper mount = getMount(path);
		OutputStream stream = append ? mount.openForAppend(path) : mount.openForWrite(path);
		if (stream != null) {
			final BufferedOutputStream writer = new BufferedOutputStream(stream);
			INormalFile file = new INormalFile() {
				@MergeVisitor.Rewrite
				protected boolean ANNOTATION;

				@Override
				public byte[] readLine() throws IOException {
					throw new UnsupportedOperationException();
				}

				@Override
				public byte[] readAll() throws IOException {
					throw new UnsupportedOperationException();
				}

				@Override
				public void write(byte[] data, int start, int length, boolean newLine) throws IOException {
					writer.write(data, start, length);
					if (newLine) writer.write('\n');
				}

				@Override
				public void close() throws IOException {
					removeFile(this, writer);
				}

				@Override
				public void flush() throws IOException {
					writer.flush();
				}
			};
			addFile(file);
			return file;
		}
		return null;
	}

	public synchronized IMountedFileBinary openForBinaryRead(String path) throws FileSystemException {
		path = sanitizePath(path);
		MountWrapper mount = this.getMount(path);
		final InputStream stream = mount.openForRead(path);
		if (stream != null) {
			IMountedFileBinary file = new IMountedFileBinary() {
				public int read() throws IOException {
					return stream.read();
				}

				public void write(int i) throws IOException {
					throw new UnsupportedOperationException();
				}

				public void close() throws IOException {
					removeFile(this, stream);
				}

				public void flush() throws IOException {
					throw new UnsupportedOperationException();
				}
			};
			addFile(file);
			return file;
		} else {
			return null;
		}
	}

	public synchronized IMountedFileBinary openForBinaryWrite(String path, boolean append) throws FileSystemException {
		path = sanitizePath(path);
		MountWrapper mount = this.getMount(path);
		final OutputStream stream = append ? mount.openForAppend(path) : mount.openForWrite(path);
		if (stream != null) {
			IMountedFileBinary file = new IMountedFileBinary() {
				public int read() throws IOException {
					throw new UnsupportedOperationException();
				}

				public void write(int i) throws IOException {
					stream.write(i);
				}

				public void close() throws IOException {
					removeFile(this, stream);
				}

				public void flush() throws IOException {
					stream.flush();
				}
			};
			addFile(file);
			return file;
		} else {
			return null;
		}
	}
}
