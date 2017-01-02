package org.squiddev.cctweaks.lua.patch;

import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.*;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.patch.iface.FileSystemPatched;
import org.squiddev.cctweaks.lua.patch.iface.MountedNormalFilePatched;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * - Improve fs.find performance (https://github.com/dan200/ComputerCraft/issues/89)
 * - Implement {@link FileSystemPatched} to allow direct access to Input/Output streams.
 * - Track open files to allow limiting the number of handles.
 * - Add binary support for file handles.
 */
@MergeVisitor.Rename(
	from = "org/squiddev/cctweaks/lua/patch/LuaExceptionStub",
	to = "dan200/computercraft/api/lua/LuaException"
)
@SuppressWarnings({"ConstantConditions", "SynchronizeOnNonFinalField"})
public class FileSystem_Patch extends FileSystem implements FileSystemPatched {
	@MergeVisitor.Stub
	private Set<IMountedFile> m_openFiles;

	private int openFilesCount;

	@MergeVisitor.Stub
	public FileSystem_Patch() throws FileSystemException {
		super(null, null);
	}

	/**
	 * Performance increases to find.
	 *
	 * If there is no wildcard then just check the file exists. Otherwise, start scanning from the
	 * last directory before the wildcard.
	 *
	 * @param wildPath The wildcard to match
	 * @return The list of found paths.
	 * @throws FileSystemException Just generally
	 */
	public synchronized String[] find(String wildPath) throws FileSystemException {
		wildPath = sanitizePath(wildPath, true);

		// If we don't have a wildcard at all just check the file exists
		int starIndex = wildPath.indexOf('*');
		if (starIndex == -1) {
			return exists(wildPath) ? new String[]{wildPath} : new String[0];
		}

		// Find the all non-wildcarded directories. For instance foo/bar/baz* -> foo/bar
		int prevDir = wildPath.substring(0, starIndex).lastIndexOf('/');
		String startDir = prevDir == -1 ? "" : wildPath.substring(0, prevDir);

		// If this isn't a directory then just abort
		if (!isDir(startDir)) return new String[0];

		// Scan as normal, starting from this directory
		Pattern wildPattern = Pattern.compile("^\\Q" + wildPath.replaceAll("\\*", "\\\\E[^\\\\/]*\\\\Q") + "\\E$");
		ArrayList<String> matches = new ArrayList<String>();
		findIn(startDir, matches, wildPattern);
		String[] array = new String[matches.size()];
		matches.toArray(array);
		return array;
	}

	/**
	 * Implementation for {@link FileSystemPatched}
	 */
	@Override
	public InputStream openForReadStream(String path) throws FileSystemException {
		path = sanitizePath(path);
		MountWrapper mount = getMount(path);
		return mount.openForRead(path);
	}

	/**
	 * Implementation for {@link FileSystemPatched}
	 */
	@Override
	public OutputStream openForWriteStream(String path) throws FileSystemException {
		path = sanitizePath(path);
		MountWrapper mount = getMount(path);
		return mount.openForWrite(path);
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

	public synchronized IMountedFileNormal openForRead(String path) throws FileSystemException {
		path = sanitizePath(path);
		MountWrapper mount = getMount(path);
		InputStream stream = mount.openForRead(path);
		if (stream != null) {
			final BufferedInputStream reader = new BufferedInputStream(stream);
			MountedNormalFilePatched file = new MountedNormalFilePatched() {
				@MergeVisitor.Rewrite
				protected boolean ANNOTATION;

				@Override
				public byte[] readLineByte() throws IOException {
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
				public byte[] readAllByte() throws IOException {
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

	public synchronized MountedNormalFilePatched openForWrite(String path, boolean append) throws FileSystemException {
		path = sanitizePath(path);
		MountWrapper mount = getMount(path);
		OutputStream stream = append ? mount.openForAppend(path) : mount.openForWrite(path);
		if (stream != null) {
			final BufferedOutputStream writer = new BufferedOutputStream(stream);
			MountedNormalFilePatched file = new MountedNormalFilePatched() {
				@MergeVisitor.Rewrite
				protected boolean ANNOTATION;

				@Override
				public byte[] readLineByte() throws IOException {
					throw new UnsupportedOperationException();
				}

				@Override
				public byte[] readAllByte() throws IOException {
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
				@MergeVisitor.Rewrite
				protected boolean ANNOTATION;

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
				@MergeVisitor.Rewrite
				protected boolean ANNOTATION;

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

	@MergeVisitor.Stub
	private void findIn(String dir, List<String> matches, Pattern wildPattern) throws FileSystemException {
	}

	@MergeVisitor.Stub
	private static String sanitizePath(String path) {
		return null;
	}

	@MergeVisitor.Stub
	private static String sanitizePath(String path, boolean allowWildcards) {
		return null;
	}

	@MergeVisitor.Stub
	private MountWrapper getMount(String path) throws FileSystemException {
		return null;
	}

	@MergeVisitor.Stub
	private static class MountWrapper {
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
}
