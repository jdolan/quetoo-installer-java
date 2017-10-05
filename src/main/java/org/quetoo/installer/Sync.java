package org.quetoo.installer;

import java.io.Closeable;
import java.io.File;

import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * A {@link Sync} synchronizes a local file structure with a remote source.
 * 
 * @author jdolan
 */
public interface Sync extends Closeable {

	/**
	 * Maps the specified {@link Asset} to a File.
	 * 
	 * @param asset The {@link Asset}.
	 * @return The File.
	 */
	File map(Asset asset);

	/**
	 * Resolves the contents of the {@link Sync}.
	 * 
	 * @return A Single emitting the Index.
	 */
	Single<Index> index();

	/**
	 * Performs a delta comparison of the {@link Sync}.
	 * 
	 * @param index The Index from which to derive the delta.
	 * @return A Single emitting the delta.
	 */
	Single<Delta> delta(Index index);

	/**
	 * Synchronizes configured destination directory.
	 * 
	 * @param delta The delta from which to synchronize {@link Asset}s.
	 * @return An Observable emitting the modified Files.
	 */
	Observable<File> sync(Delta delta);
}
