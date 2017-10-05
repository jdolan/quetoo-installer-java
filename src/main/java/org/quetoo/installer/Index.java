package org.quetoo.installer;

/**
 * An Index relates the contents of a {@link Sync}.
 * 
 * @author jdolan
 */
public interface Index extends Iterable<Asset> {

	/**
	 * @return The number of {@link Asset}s in the Index.
	 */
	int count();

	/**
	 * @return The {@link Sync}.
	 */
	Sync getSync();
}
