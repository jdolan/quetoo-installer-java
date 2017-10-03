package org.quetoo.installer;

import java.io.Closeable;
import java.io.File;

import io.reactivex.Observable;

/**
 * Syncs synchronize a local directory with a remote one.
 * 
 * @author jdolan
 */
public interface Sync extends Closeable {

	/**
	 * Maps the specified Asset to a File.
	 * 
	 * @param asset The Asset.
	 * @return The File.
	 */
	File map(Asset asset);

	/**
	 * Reads the remote source.
	 * 
	 * @return An Observable yielding the contents of the source.
	 */
	Observable<Asset> read();

	/**
	 * Performs a delta comparison of the remote source.
	 * 
	 * @return An Observable yielding the delta result.
	 */
	Observable<Asset> delta();

	/**
	 * Synchronizes the remote source to the configured destination.
	 * 
	 * @return An Observable yielding the Files synchronized from the remote source.
	 */
	Observable<File> sync();
}
