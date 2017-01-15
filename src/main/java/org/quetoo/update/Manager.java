package org.quetoo.update;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.quetoo.update.aws.S3BucketSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Observable;

/**
 * The update manager.
 * 
 * @author jdolan
 */
public class Manager {
	
	private final Config config;

	private final Observable<Sync> syncs;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/**
	 * Instantiates a {@link Manager} with the specified {@link Config}.
	 * 
	 * @param config The configuration.
	 */
	public Manager(final Config config) {
		this.config = config;
		
		syncs = Observable.fromArray(
				
			new S3BucketSync.Builder()
				.withAmazonS3Client(config.getAmazonS3Client())
				.withHttpClient(config.getHttpClient())
				.withBucketName("quetoo")
				.withPredicate(s -> s.getKey().startsWith(config.getArchHostPrefix()))
				.withMapper(s -> new File(s.getKey().replace(config.getArchHostPrefix(), "")))
				.withDestination(config.getDir())
				.build(),
				
			new S3BucketSync.Builder()
				.withAmazonS3Client(config.getAmazonS3Client())
				.withHttpClient(config.getHttpClient())
				.withBucketName("quetoo-data")
				.withPredicate(s -> true)
				.withMapper(s -> new File(s.getKey()))
				.withDestination(config.getShare())
				.build()
		);
	}
	
	/**
	 * Prunes the configured directory to mirror the aggregate sync result.
	 * 
	 * @param files The aggregate sync result.
	 */
	private void onComplete(Set<File> files) {
		
		if (config.getPrune()) {
			FileUtils.listFiles(config.getDir(), null, true).stream().filter(f -> {;
				return !files.contains(f);
			}).forEach(f -> {
				FileUtils.deleteQuietly(f);
				
				log.info("Removed {}", f);
			});
		}
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
		
		log.info("Updated {}", file);
		
		return file;
	}
	
	/**
	 * Logs the given error.
	 * 
	 * @param t The Throwable error.
	 */
	private void onError(final Throwable t) {
		log.error(t.getMessage(), t);
	}
	
	/**
	 * Dispatches the configured {@link Sync}s.
	 * 
	 * @throws IOException If an error occurs.
	 */
	public void sync() throws IOException {
		
		Observable.merge(syncs.map(Sync::sync))
			.map(this::onSync)
			.collectInto(new HashSet<File>(), (set, file) -> set.add(file))
			.subscribe(this::onComplete, this::onError);		
	}
}
