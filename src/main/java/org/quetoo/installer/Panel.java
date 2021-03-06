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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import io.reactivex.Observable;
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

	private final List<Disposable> subscriptions = Collections.synchronizedList(new ArrayList<>());

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

		status = new JLabel("Retrieving asset list..");

		summary = new JTextArea(10, 40);
		summary.setMargin(new Insets(5, 5, 5, 5));
		summary.setEditable(false);

		summary.append("Updating " + manager.getConfig().getDir() + "\n");
		summary.append("Retrieving asset list for " + manager.getConfig().getBuild() + "..\n");

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

			panel = new JPanel();
			panel.add(copySummary, BorderLayout.EAST);

			add(panel, BorderLayout.PAGE_END);
		}

		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	}
	
	/**
	 * Cancels all pending subscriptions.
	 */
	public void cancel() {
		subscriptions.stream().forEach(Disposable::dispose);
		subscriptions.clear();
	}

	/**
	 * Dispatches {@link Manager#sync(Observable)}.
	 */
	public void update() {
		
		progressBar.setValue(0);
		progressBar.setMaximum(0);
		progressBar.setIndeterminate(true);

		Schedulers.io().scheduleDirect(() -> {				
			final Observable<File> files = manager.sync(
				manager.delta(
					manager.index()
						.toList()
						.doOnSuccess(this::onIndices)
						.flatMapObservable(Observable::fromIterable)
					).toList()
					.doOnSuccess(this::onDeltas)
					.flatMapObservable(Observable::fromIterable)
			).observeOn(Schedulers.from(SwingUtilities::invokeLater));

			subscriptions.add(files.subscribe(this::onSync, this::onError, this::onComplete));
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
	 * Called when all indices are available.
	 * 
	 * @param indices The indices.
	 */
	private void onIndices(final List<Index> indices) {

		final int count = indices.stream().mapToInt(Index::count).sum();
		setStatus("Calculating update for " + count + " assets");
	}

	/**
	 * Called when the deltas are available.
	 * 
	 * @param deltas The deltas.
	 */
	private void onDeltas(final List<Delta> deltas) {

		final int count = deltas.stream().mapToInt(Delta::count).sum();
		final long size = deltas.stream().mapToLong(Delta::size).sum();
		
		final BigDecimal megabytes = new BigDecimal(size / 1024.0 / 1024.0).setScale(2, RoundingMode.UP);

		setStatus("Updating " + count + " assets, " + megabytes + "MB");

		progressBar.setIndeterminate(false);
		progressBar.setMaximum(Math.max((int) size, 1));
	}

	/**
	 * Called when each File is synchronized.
	 * 
	 * @param file The File.
	 */
	private void onSync(final File file) {

		progressBar.setValue(progressBar.getValue() + (int) file.length());

		final String dir = manager.getConfig().getDir() + File.separator;
		final String filename = file.toString().replace(dir, "");

		setStatus(filename);
	}

	/**
	 * Called when an error occurs.
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
	 * Called when the sync operation completes successfully.
	 */
	private void onComplete() {
		
		setStatus("Update complete");
		
		progressBar.setValue(progressBar.getMaximum());
				
		Schedulers.io().scheduleDirect(() -> {
			final Disposable prune = manager.prune()
					.observeOn(Schedulers.from(SwingUtilities::invokeLater))
					.subscribe(this::onPrune, this::onError);
			subscriptions.add(prune);
		});
	}
	
	/**
	 * Called when each File is pruned.
	 * 
	 * @param file The File.
	 */
	private void onPrune(final File file) {
		
		final String dir = manager.getConfig().getDir() + File.separator;
		final String filename = file.toString().replace(dir, "");
		
		if (manager.getConfig().getPrune()) {
			setStatus("Removed unknown asset " + filename);
		} else {
			setStatus("Unknown asset " + filename);
		}
	}

	/**
	 * Copies the contents of `summary` to the clipboard.
	 */
	private void onCopySummary(final ActionEvent e) {
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(summary.getText()), null);
	}
}
