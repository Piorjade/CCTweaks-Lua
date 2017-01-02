package org.squiddev.cctweaks.lua.patch;

import dan200.computercraft.core.apis.FSAPI;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.filesystem.IMountedFileNormal;
import org.squiddev.cctweaks.lua.lib.fs.ReaderObject;
import org.squiddev.cctweaks.lua.lib.fs.WriterObject;

/**
 * Use our custom reader/writer objects instead
 */
public class FSAPI_Patch extends FSAPI {
	public FSAPI_Patch(IAPIEnvironment _env) {
		super(_env);
	}

	private static Object[] wrapBufferedWriter(final IMountedFileNormal writer) {
		return new Object[]{new WriterObject(writer)};
	}

	private static Object[] wrapBufferedReader(final IMountedFileNormal writer) {
		return new Object[]{new ReaderObject(writer)};
	}
}
