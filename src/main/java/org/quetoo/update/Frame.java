package org.quetoo.update;

import java.io.IOException;

import javax.swing.JFrame;

/**
 * The top level container for the user interface.
 * 
 * @author jdolan
 */
public class Frame extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Manager manager;
	private final Panel panel;

	/**
	 * Instantiates a {@link Frame} with the specified {@link Config}.
	 * 
	 * @param config The Config.
	 */
	public Frame(final Config config) {
		super(Config.NAME + " " + Config.VERSION);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new Panel(config);

		setContentPane(panel);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);

		manager = new Manager(config);

		try {
			manager.sync().subscribe(panel::onSync, panel::onError, panel::onComplete);
		} catch (IOException ioe) {

		}
	}
}
