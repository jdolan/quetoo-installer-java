package org.quetoo.installer;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.quetoo.installer.aws.S3BucketSync;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * The manager.
 * 
 * @author jdolan
 */
public class Manager {
	
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
	 * 
	 * @return An Observable yielding the aggregate delta result.
	 */
	public Observable<Asset> delta() {
		return Observable.merge(syncs.map(Sync::delta));
	}

	/**
	 * @return An Observable yielding the aggregate sync result.
	 */
	public Observable<File> sync() {
		return Observable.merge(syncs.map(Sync::sync))
				.doOnNext(file -> {
					if (file.getParentFile().equals(config.getBin())) {
						file.setExecutable(true);
					}
				});
	}

	/**
	 * Prunes the destination directory, purging files not present in the configured Syncs.
	 * 
	 * @return A Completable yielding the prune.
	 */
	public Completable prune() {
		return Observable.merge(syncs.map(sync -> sync.read().map(sync::map)))
				.toList()
				.doOnSuccess(files -> {
					if (config.getPrune()) {
						FileUtils.listFiles(config.getDir(), null, true).stream().filter(file -> {
							return !files.contains(file);
						}).forEach(file -> {
							FileUtils.deleteQuietly(file);
						});
					}
				}).toCompletable();
	}
	
	/**
	 * @return This Manager's {@link Config}.
	 */
	public Config getConfig() {
		return config;
	}
}
