package org.squiddev.cctweaks.lua;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamHelpers {
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		while (true) {
			int r = input.read(buf);
			if (r == -1) {
				break;
			}
			output.write(buf, 0, r);
		}

		return output.toByteArray();
	}
}
