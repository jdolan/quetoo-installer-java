package org.quetoo.installer;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;

/**
 * The console user interface.
 * 
 * @author jdolan
 */
public class Console {

	private final Manager manager;

	/**
	 * Instantiates a Console to dispatch and await the given {@link Manager}.
	 * 
	 * @param manager The {@link Manager}.
	 * @throws IOException
	 */
	public Console(final Manager manager) {
		this.manager = manager;
		sync();
	}

	/**
	 * Dispatches {@link Manager#sync()}.
	 */
	public void sync() {
		try {
			final Observable<File> files = manager.sync();
			files.subscribe(this::onSync, this::onError, this::onComplete);
			files.toList().blockingGet();
		} catch (IOException ioe) {
			onError(ioe);
		}
	}

	/**
	 * Logs the sync progress of `file`.
	 * 
	 * @param file The newly synced File.
	 */
	private void onSync(final File file) {
		System.out.println("Updated " + file);
	}

	/**
	 * Logs the sync completion.
	 */
	private void onComplete() {
		System.out.println("Complete");
	}

	/**
	 * Logs the specified error to `stderr`.
	 * 
	 * @param throwable The Throwable error.
	 */
	private void onError(final Throwable throwable) {
		throwable.printStackTrace(System.err);
	}
}
