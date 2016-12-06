package org.squiddev.cctweaks.lua.patch;

import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Patches FileSystem to improve fs.find performance.
 *
 * @link https://github.com/dan200/ComputerCraft/issues/89.
 *
 * Also implement {@link IPatchedFileSystem} to allow direct access to Input/Output streams.
 */
public class FileSystem_Patch extends FileSystem implements IPatchedFileSystem {
	@MergeVisitor.Stub
	public FileSystem_Patch() throws FileSystemException {
		super(null, null);
	}

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

	@Override
	public InputStream openForReadStream(String path) throws FileSystemException {
		path = sanitizePath(path);
		MountWrapper mount = this.getMount(path);
		return mount.openForRead(path);
	}

	@Override
	public OutputStream openForWriteStream(String path) throws FileSystemException {
		path = sanitizePath(path);
		MountWrapper mount = this.getMount(path);
		return mount.openForWrite(path);
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
	}
}
