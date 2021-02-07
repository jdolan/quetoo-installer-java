package org.quetoo.installer;

import java.io.File;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.quetoo.installer.aws.S3Sync;

import io.reactivex.Observable;

/**
 * The manager.
 * 
 * @author jdolan
 */
public class Manager {
	
	private final Config config;
	private final Sync quetoo, quetooData;
		
	/**
	 * Instantiates a {@link Manager} with the specified {@link Config}.
	 * 
	 * @param config The configuration.
	 */
	public Manager(final Config config) {
		this.config = config;
		
		quetoo = new S3Sync.Builder()
				.withHttpClient(config.getHttpClient())
				.withBucketName("quetoo")
				.withPredicate(s -> s.getKey().startsWith(config.getArchHostPrefix()))
				.withMapper(s -> new File(s.getKey().replace(config.getArchHostPrefix(), "")))
				.withDestination(config.getDir())
				.build();
				
		quetooData = new S3Sync.Builder()
				.withHttpClient(config.getHttpClient())
				.withBucketName("quetoo-data")
				.withPredicate(s -> true)
				.withMapper(s -> new File(s.getKey()))
				.withDestination(config.getData())
				.build();
	}
	
	/**
	 * Fetches the merged indices from the configured {@link Sync}s.
	 * 
	 * @return The merged indices.
	 */
	public Observable<Index> index() {
		return Observable.merge(quetoo.index(), quetooData.index());
	}
	
	/**
	 * Calculates the merged delta from the given indices.
	 * 
	 * @param indices The merged indices.
	 * @return The merged deltas.
	 */
	public Observable<Delta> delta(final Observable<Index> indices) {
		return indices.flatMapSingle(index -> index.getSync().delta(index));
	}

	/**
	 * Synchronizes the destination directory using the given merged deltas.
	 * 
	 * @param deltas The merged deltas.
	 * @return An Observable yielding the synchronized files.
	 */
	public Observable<File> sync(final Observable<Delta> deltas) {
		return deltas.flatMap(delta -> delta.getIndex().getSync().sync(delta))
				.doOnNext(file -> {
					if (file.getParentFile().equals(config.getBin())) {
						file.setExecutable(true);
					}
				});
	}

	/**
	 * Prunes the destination directory, purging files not present in the specified indices.
	 * 
	 * @return An Observable yielding the pruned files.
	 */
	public Observable<File> prune() {
		return index().flatMapIterable(index -> index)
				.map(asset -> asset.getIndex().getSync().map(asset))
				.toList()
				.map(files ->
					FileUtils.listFiles(config.getDir(), null, true).stream()
						.filter(file -> !files.contains(file))
						.collect(Collectors.toList()))
				.flatMapObservable(Observable::fromIterable)
				.doOnNext(file -> {
					if (config.getPrune()) {
						FileUtils.deleteQuietly(file);
					}
				});
	}
	
	/**
	 * @return This Manager's {@link Config}.
	 */
	public Config getConfig() {
		return config;
	}
}
