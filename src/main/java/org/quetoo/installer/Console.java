package org.quetoo.installer;

import java.io.IOException;
import java.util.List;

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
		
		System.out.println("Updating " + manager.getConfig().getDir());
		
		try {
			final List<Asset> delta = manager.delta().toList().blockingGet();
			
			final long size = delta.stream().mapToLong(Asset::size).sum();
			System.out.println("Fetching " + delta.size() + " assets (" + size + " bytes)");
			
			manager.sync(Observable.fromIterable(delta))
					.doOnNext(System.out::println)
					.blockingSubscribe();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			System.exit(1);
		}

		System.out.println("Update complete");
	}
}
