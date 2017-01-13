package org.quetoo.update;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;

/**
 * 
 * @author jdolan
 *
 */
public interface Sync {
	
	/**
	 * Synchronizes the remote source to the configured destination.
	 * 
	 * @return A List of all Files synchronized from the remote source.
	 * @throws IOException If an error occurs.
	 */
	public Observable<File> sync() throws IOException;

}
