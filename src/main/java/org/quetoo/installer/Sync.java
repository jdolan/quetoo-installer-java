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
	 * A listener interface for the Sync.
	 */
	interface Listener {
		
		/**
		 * Called when the Sync source has been read to setup progress reporting.
		 * 
		 * @param count The number of items to sync.
		 * @param size The total size of the items to sync in bytes.
		 */
		void onRead(int count, int size);	
	}
		
	/**
	 * Cancels any pending sync operation.
	 */
	void cancel();
	
	/**
	 * Synchronizes the remote source to the configured destination.
	 * 
	 * @return A List of all Files synchronized from the remote source.
	 * @throws IOException If an error occurs.
	 */
	Observable<File> sync() throws IOException;
}
