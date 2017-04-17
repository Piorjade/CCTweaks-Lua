package org.squiddev.cctweaks.api.lua;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Create an instance of {@link ILuaAPI} for a given computer
 */
public interface ILuaAPIFactory {
	/**
	 * Create the API
	 *
	 * @param computer The computer to create for
	 * @return The created API.
	 */
	@Nullable
	ILuaAPI create(@Nonnull IExtendedComputerAccess computer);

	/**
	 * Globals to export this API as
	 *
	 * @return The API to export under
	 */
	@Nonnull
	String[] getNames();
}
