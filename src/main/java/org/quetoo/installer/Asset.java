package org.quetoo.installer;

/**
 * An abstraction for a remote resource.
 */
public interface Asset {
	
	/**
	 * @return The Sync providing this Asset.
	 */
	Sync source();

	/**
	 * @return The Asset name.
	 */
	String name();

	/**
	 * @return The Asset size in bytes.
	 */
	long size();

	/**
	 * @return True if the Asset is a directory, false otherwise.
	 */
	boolean isDirectory();
}
