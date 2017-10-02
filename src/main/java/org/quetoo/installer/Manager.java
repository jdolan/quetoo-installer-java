package org.quetoo.installer;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.quetoo.installer.aws.S3BucketSync;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * The manager.
 * 
 * @author jdolan
 */
public class Manager {
	
	private static final Consumer<Asset> noop = asset -> {};

	private final Config config;
	private final Observable<S3BucketSync> syncs;
		
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
	 * Dispatches all configured Syncs and merges their result.
	 * 
	 * @param onRead An optional Consumer for read events.
	 * @param onDelta An optional Consumer for delta events.
	 * @param onSync An optional Consumer for sync events.
	 * @return An Observable yielding the merged sync result.
	 */
	public Observable<File> sync(
			final Consumer<Asset> onRead,
			final Consumer<Asset> onDelta,
			final Consumer<Asset> onSync) {

		final Observable<File> files = Observable
				.merge(syncs.map(s -> s.sync(
						onRead != null ? onRead : noop,
						onDelta != null ? onDelta : noop,
						onSync != null ? onSync : noop
				))).share();

		files.toList().subscribe(this::postProcess);
		return files;
	}

	/**
	 * Post-processes the aggregate sync result.
	 * @param files The aggregate sync result.
	 */
	private void postProcess(List<File> files) {
		
		for (File file : files) {
			if (file.getParentFile().equals(config.getBin())) {
				file.setExecutable(true);
			}
		}
		
		if (config.getPrune()) {
			FileUtils.listFiles(config.getDir(), null, true).stream().filter(file -> {
				return !files.contains(file);
			}).forEach(file -> {
				FileUtils.deleteQuietly(file);
			});
		}		
	}
	
	/**
	 * @return This Manager's {@link Config}.
	 */
	public Config getConfig() {
		return config;
	}
}
