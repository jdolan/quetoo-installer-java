package org.quetoo.update;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.quetoo.update.aws.S3BucketSync;

import io.reactivex.Observable;

/**
 * The update manager.
 * 
 * @author jdolan
 */
public class Manager {
	
	private final Config config;

	private final Observable<Sync> syncs;
	
	private Boolean isCancelled;
		
	/**
	 * Instantiates a {@link Manager} with the specified {@link Config}.
	 * 
	 * @param config The configuration.
	 */
	public Manager(final Config config) {
		this.config = config;
		
		syncs = Observable.fromArray(
				
			new S3BucketSync.Builder()
				.withHttpClient(config.getHttpClient())
				.withBucketName("quetoo")
				.withPredicate(s -> s.getKey().startsWith(config.getArchHostPrefix()))
				.withMapper(s -> new File(s.getKey().replace(config.getArchHostPrefix(), "")))
				.withDestination(config.getDir())
				.build(),
				
			new S3BucketSync.Builder()
				.withHttpClient(config.getHttpClient())
				.withBucketName("quetoo-data")
				.withPredicate(s -> true)
				.withMapper(s -> new File(s.getKey()))
				.withDestination(config.getShare())
				.build()
		);
	}
	
	/**
	 * Modifies permissions on the newly synced File.
	 * 
	 * @param file The newly synced File.
	 * 
	 * @return The File.
	 */
	private File onSync(final File file) {
		
		if (file.getParentFile().equals(config.getBin())) {
			file.setExecutable(true);
		}
		
		System.out.println("Updated " + file);
		return file;
	}
	
	/**
	 * Prunes the configured directory to mirror the aggregate sync result.
	 * 
	 * @param files The aggregate sync result.
	 */
	private void onComplete(Set<File> files) {
		
		if (config.getPrune() && !isCancelled) {
			FileUtils.listFiles(config.getDir(), null, true).stream().filter(file -> {
				return !files.contains(file);
			}).forEach(file -> {
				FileUtils.deleteQuietly(file);
				System.out.println("Removed " + file);
			});
		}
		
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
	
	/**
	 * Cancels all pending sync operations.
	 */
	public void cancel() {
		isCancelled = true;
		syncs.forEach(Sync::cancel);
	}
	
	/**
	 * Dispatches the configured {@link Sync}s.
	 * 
	 * @return An Observable of the merged {@link Sync} result.
	 * 
	 * @throws IOException If an error occurs.
	 */
	public Observable<File> sync() throws IOException {

		Observable<File> files = Observable.merge(syncs.map(Sync::sync)).map(this::onSync);

		files.doOnError(this::onError);

		files.collectInto(new HashSet<File>(), (set, file) -> set.add(file)).subscribe(this::onComplete);

		return files;
	}
}
