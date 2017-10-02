package org.quetoo.installer;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * The primary container of the user interface.
 * 
 * @author jdolan
 */
public class Panel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final Manager manager;
	private final JProgressBar progressBar;
	private final JLabel status;
	private final JTextArea summary;
	private final JButton copySummary;
	private final JButton cancel;

	private Disposable disposable;

	/**
	 * Instantiates a {@link Panel} with the specified {@link Manager}.
	 * 
	 * @param manager The Manager.
	 */
	public Panel(final Manager manager) {

		super(new BorderLayout(0, 5), true);

		this.manager = manager;

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		status = new JLabel("Retrieving objects list..");

		summary = new JTextArea(10, 40);
		summary.setMargin(new Insets(5, 5, 5, 5));
		summary.setEditable(false);

		summary.append("Updating " + manager.getConfig().getDir() + "\n");

		final String prefix = manager.getConfig().getArchHostPrefix();
		summary.append("Retrieving objects list for " + prefix + "..\n");

		DefaultCaret caret = (DefaultCaret) summary.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		{
			JPanel panel = new JPanel();

			panel.setLayout(new BorderLayout(0, 5));

			panel.add(status, BorderLayout.NORTH);
			panel.add(progressBar, BorderLayout.SOUTH);

			panel.setSize(panel.getPreferredSize());

			add(panel, BorderLayout.PAGE_START);
		}

		add(new JScrollPane(summary), BorderLayout.CENTER);

		{
			JPanel panel = new JPanel(new BorderLayout(0, 5));

			copySummary = new JButton("Copy Summary");
			copySummary.addActionListener(this::onCopySummary);

			cancel = new JButton("Cancel");
			cancel.addActionListener(this::onCancel);

			panel = new JPanel();
			panel.add(copySummary, BorderLayout.WEST);
			panel.add(cancel, BorderLayout.EAST);

			add(panel, BorderLayout.PAGE_END);
		}

		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	}

	/**
	 * Dispatches {@link Manager#sync()}.
	 */
	public void sync() {

		if (disposable != null) {
			disposable.dispose();
			disposable = null;
		}
		
		progressBar.setValue(0);
		progressBar.setMaximum(0);
		
		Schedulers.io().scheduleDirect(() -> {
			disposable = manager.sync(null, this::onDelta, this::onSync)
					.observeOn(Schedulers.from(SwingUtilities::invokeLater))
					.subscribe(this::onFile, this::onError, this::onComplete);
		});
	}

	/**
	 * Sets the status to `string` and appends `string` to the summary.
	 * 
	 * @param string The String to log.
	 */
	private void setStatus(final String string) {

		status.setText(string);
		summary.append(string + "\n");
	}

	/**
	 * Updates the progress bar to include the target size of `asset`.
	 * 
	 * @param asset The delta Asset.
	 */
	private void onDelta(final Asset asset) {
		progressBar.setMaximum(progressBar.getMaximum() + (int) asset.size());
	}

	/**
	 * Updates the progress bar to include the progress for `asset`.
	 * 
	 * @param asset The newly synced Asset.
	 */
	private void onSync(final Asset asset) {
		progressBar.setValue(progressBar.getValue() + (int) asset.size());
	}

	/**
	 * Updates the interface components to reflect the newly synced File.
	 * 
	 * @param file The newly synced File.
	 */
	private void onFile(final File file) {

		final String dir = manager.getConfig().getDir() + File.separator;
		final String filename = file.toString().replace(dir, "");

		setStatus(filename);
	}

	/**
	 * Updates the interface components to reflect the error.
	 * 
	 * @param throwable The error.
	 */
	private void onError(final Throwable throwable) {

		status.setText(throwable.getMessage());

		final StringWriter stackTrace = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stackTrace));

		summary.append(stackTrace.toString());
	}

	/**
	 * Updates the interface components to reflect completion.
	 */
	private void onComplete() {
		setStatus("Update complete");
		progressBar.setValue(progressBar.getMaximum());
		cancel.setEnabled(false);
	}

	/**
	 * Copies the contents of `summary` to the clipboard.
	 */
	private void onCopySummary(final ActionEvent e) {
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(summary.getText()), null);
	}

	/**
	 * Cancels the sync operation.
	 */
	private void onCancel(final ActionEvent e) {
		if (disposable != null) {
			setStatus("Cancelling..");
			disposable.dispose();
		}
	}
}
