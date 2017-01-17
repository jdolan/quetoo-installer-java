package org.quetoo.update;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

/**
 * The primary container of the user interface.
 * 
 * @author jdolan
 */
public class Panel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final Config config;
	private final JProgressBar progressBar;
	private final JLabel status;
	private final JTextArea summary;
	private final JButton cancel;

	/**
	 * Instantiates a {@link Panel} with the specified {@link Config}.
	 * 
	 * @param config The Config.
	 */
	public Panel(final Config config) {

		super(new BorderLayout(), true);

		this.config = config;

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		status = new JLabel();

		summary = new JTextArea(10, 40);
		summary.setMargin(new Insets(5, 5, 5, 5));
		summary.setEditable(false);

		DefaultCaret caret = (DefaultCaret) summary.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		{
			JPanel panel = new JPanel();

			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.add(progressBar);
			panel.add(status);

			add(panel, BorderLayout.PAGE_START);
		}

		add(new JScrollPane(summary), BorderLayout.CENTER);

		{
			JPanel panel = new JPanel();

			cancel = new JButton("Cancel");

			panel = new JPanel();
			panel.add(cancel);

			add(panel, BorderLayout.PAGE_END);
		}

		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	}

	/**
	 * Updates the interface components to reflect the newly synced File.
	 * 
	 * @param file The newly synced File.
	 */
	public void onSync(final File file) {

		final String dir = config.getDir() + File.separator;
		final String filename = file.toString().replace(dir, "");

		status.setText(filename);
		summary.append(filename + "\n");

		progressBar.setValue(progressBar.getValue() + 1);
	}

	/**
	 * Updates the interface components to reflect the error.
	 * 
	 * @param throwable The error.
	 */
	public void onError(final Throwable throwable) {

		status.setText(throwable.getMessage());

		final StringWriter stackTrace = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stackTrace));
		summary.append(stackTrace.toString());
	}

	/**
	 * Updates the interface components to reflect completion.
	 */
	public void onComplete() {
		status.setText("");
		summary.append("Complete\n");
		progressBar.setValue(progressBar.getMaximum());
		cancel.setEnabled(false);
	}
}
