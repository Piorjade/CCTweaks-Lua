package org.squiddev.cctweaks.lua.lib.socket;

public interface ISocketListener {
	/**
	 * Fired when the socket is closed
	 */
	void onClosed();

	/**
	 * Fired when there is more data to read
	 */
	void onMessage();

	/**
	 * Fired when the connection is finished/ready
	 */
	void onConnectFinished();
}
