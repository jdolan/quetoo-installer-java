package org.quetoo.installer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * The top level container for the user interface.
 * 
 * @author jdolan
 */
public class Frame extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Panel panel;

	/**
	 * Instantiates a {@link Frame} with the specified {@link Manager}
	 * 
	 * @param manager The Manager.
	 */
	public Frame(final Manager manager) {

		super(Config.NAME + " " + Config.VERSION);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new Panel(manager);

		setContentPane(panel);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);

		SwingUtilities.invokeLater(panel::sync);
	}
}
