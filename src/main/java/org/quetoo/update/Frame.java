package org.quetoo.update;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang3.SystemUtils;

/**
 * The top level container for the user interface.
 * 
 * @author jdolan
 */
public class Frame extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Manager manager;
	private final Panel panel;

	static {
		if (SystemUtils.IS_OS_MAC_OSX) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * Instantiates a {@link Frame} with the specified {@link Config}, and dispatches a
	 * {@link Manager} to initiate the sync process.
	 * 
	 * @param config The Config.
	 */
	public Frame(final Config config) {
		super(Config.NAME + " " + Config.VERSION);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		manager = new Manager(config);

		panel = new Panel(manager);

		setContentPane(panel);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);

		SwingUtilities.invokeLater(panel::sync);
	}
}
