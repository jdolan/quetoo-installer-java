package org.quetoo.installer;

import java.io.IOException;

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
			manager.sync(null, null, this::onSync).blockingSubscribe();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			System.exit(1);
		}

		System.out.println("Complete");
	}

	/**
	 * Logs the sync progress of `asset`.
	 * 
	 * @param file The newly synced Asset.
	 */
	private void onSync(final Asset asset) {
		System.out.println("Updated " + asset);
	}
}
