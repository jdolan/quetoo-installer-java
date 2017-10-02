package org.quetoo.installer;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.quetoo.installer.aws.S3BucketSync;

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
	 * Dispatches all configured Syncs and merges their result.
	 * 
	 * @param delta The aggregate delta.
	 * @return An Observable yielding the merged sync result.
	 */
	public Observable<File> sync(final Observable<Asset> delta) {
		return Observable.merge(syncs.map(sync -> sync.sync(delta)))
				.doOnNext(file -> {
					if (file.getParentFile().equals(config.getBin())) {
						file.setExecutable(true);
					}
				})
				.doOnComplete(() -> {
					
				});
	}

	/**
	 * Post-processes the aggregate sync result.
	 * @param files The aggregate sync result.
	 */
	private void prune(List<File> files) {
		
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
