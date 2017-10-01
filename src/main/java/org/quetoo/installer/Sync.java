package org.quetoo.installer;

import java.io.Closeable;
import java.io.File;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * Syncs synchronize a local directory with a remote one.
 * 
 * @author jdolan
 */
public interface Sync extends Closeable {

	/**
	 * Synchronizes the remote source to the configured destination.
	 * 
	 * @param onRead A Consumer for read events.
	 * @param onDelta A Consumer for delta events.
	 * @param onSync A Consumer for sync events.
	 * @return An Observable yielding the Files synchronized from the remote source.
	 */
	Observable<File> sync(Consumer<Asset> onRead, Consumer<Asset> onDelta, Consumer<Asset> onSync);
}
