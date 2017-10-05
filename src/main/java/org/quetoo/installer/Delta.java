package org.quetoo.installer;

/**
 * A delta is the subset of an {@link Index} that appears out of date.
 * 
 * @author jdolan
 */
public interface Delta extends Iterable<Asset> {

	/**
	 * @return The number of {@link Asset}s in the delta.
	 */
	int count();
	
	/**
	 * @return The size, in bytes, of the delta.
	 */
	long size();

	/**
	 * @return The index on which this delta is based.
	 */
	Index getIndex();
}
