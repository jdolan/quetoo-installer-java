package org.quetoo.update;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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

	private final List<Sync> syncs;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/**
	 * Instantiates a {@link Manager} with the specified {@link Config}.
	 * 
	 * @param config The configuration.
	 */
	public Manager(final Config config) {
		this.config = config;
		
		syncs = new ArrayList<>();
		
		syncs.add(new S3BucketSync.Builder()
				.withAmazonS3Client(config.getAmazonS3Client())
				.withHttpClient(config.getHttpClient())
				.withBucketName("quetoo")
				.withPredicate(s -> s.getKey().startsWith(config.getArchHostPrefix()))
				.withMapper(s -> new File(s.getKey().replace(config.getArchHostPrefix(), "")))
				.withDestination(config.getDir())
				.build());
		
		syncs.add(new S3BucketSync.Builder()
				.withAmazonS3Client(config.getAmazonS3Client())
				.withHttpClient(config.getHttpClient())
				.withBucketName("quetoo-data")
				.withPredicate(s -> true)
				.withMapper(s -> new File(s.getKey()))
				.withDestination(config.getShare())
				.build());
	}
	
	/**
	 * Prunes the configured directory to mirror the aggregate sync result.
	 * 
	 * @param files The aggregate sync result.
	 */
	private void prune(Set<File> files) {
		
		if (config.getPrune()) {
			FileUtils.listFiles(config.getDir(), null, true).stream().filter(file -> {;
				return !files.contains(file);
			}).forEach(file -> {
				FileUtils.deleteQuietly(file);
				log.info("Removed {}", file);
			});
		}
	}
	
	private void onNext(final File file) {
		log.info("Updated {}", file);
	}
	
	private void onError(final Throwable t) {
		log.error(t.getMessage(), t);
	}
	
	/**
	 * Dispatches the configured {@link Sync}s.
	 * 
	 * @throws IOException If an error occurs.
	 */
	public void sync() throws IOException {
		
		Observable<File> files = Observable.merge(
			syncs.stream().map(s -> safe(() -> s.sync())).collect(Collectors.toList())
		);
		
		files.subscribe(this::onNext, this::onError);
		
		files.collectInto(new HashSet<File>(), (set, file) -> {
			set.add(file);
		}).subscribe(this::prune);
	}
	
	/**
	 * Invokes the callable, re-throwing any checked Exceptions as RuntimeExceptions.
	 * 
	 * @param <T> The Callable result type.
	 * @param callable A Callable.
	 * @return The Callable result.
	 */
	public static <T> T safe(Callable<T> callable) {
		try {
			return callable.call();
		} catch (Throwable t) {
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			} else {
				throw new RuntimeException(t);
			}
		}
	}
}
