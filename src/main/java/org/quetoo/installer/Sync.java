package org.quetoo.installer;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;

/**
 * Syncs synchronize a local directory with a remote one.
 * 
 * @author jdolan
 */
public interface Sync {
		
	/**
	 * Cancels any pending sync operation.
	 */
	public void cancel();
	
	/**
	 * Synchronizes the remote source to the configured destination.
	 * 
	 * @return A List of all Files synchronized from the remote source.
	 * @throws IOException If an error occurs.
	 */
	public Observable<File> sync() throws IOException;
}
