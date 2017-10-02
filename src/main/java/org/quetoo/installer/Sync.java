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
	 * Performs a delta comparison of the remote source.
	 * 
	 * @return An Observable yielding the delta result.
	 */
	Observable<Asset> delta();

	/**
	 * Synchronizes the remote source to the configured destination.
	 * 
	 * @param delta The Assets to sync.
	 * @return An Observable yielding the Files synchronized from the remote source.
	 */
	Observable<File> sync(Observable<Asset> delta);
}
