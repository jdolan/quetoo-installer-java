package org.quetoo.update;

import java.io.File;
import java.io.IOException;
import java.util.Set;


/**
 * 
 * @author jdolan
 *
 */
public interface Sync {
	
	/**
	 * A listener for observing {@link Sync} instances.
	 */
	public interface Listener {

		/**
		 * @param count The count of objects to be synchronized.
		 */
		void onCountObjects(int count);

		/**
		 * @param name The object name to be synchronized.
		 */
		void onSyncObject(String name);

		/**
		 * @param file The file to be removed.
		 */
		void onRemoveFile(File file);
	}
	
	/**
	 * Synchronizes the remote source to the configured destination.
	 * 
	 * @return A List of all Files synchronized from the remote source.
	 * @throws IOException If an error occurs.
	 */
	public Set<File> sync() throws IOException;

}
