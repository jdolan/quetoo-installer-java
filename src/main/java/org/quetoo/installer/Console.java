package org.quetoo.installer;

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
			manager.sync().doOnNext(System.out::println).blockingSubscribe();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			System.exit(1);
		}

		System.out.println("Update complete");
	}
}
