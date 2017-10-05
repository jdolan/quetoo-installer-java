package org.quetoo.installer;

/**
 * Assets represent a remote resource.
 */
public interface Asset {

	/**
	 * @return The Index containing this Asset.
	 */
	Index getIndex();

	/**
	 * @return The asset name.
	 */
	String getName();

	/**
	 * @return The asset size in bytes.
	 */
	long size();

	/**
	 * @return True if the asset represents a directory, false otherwise.
	 */
	boolean isDirectory();
}
