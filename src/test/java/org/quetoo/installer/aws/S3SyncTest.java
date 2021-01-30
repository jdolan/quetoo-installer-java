package org.quetoo.installer.aws;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

/**
 * Integration tests for the {@link S3Sync} class.
 * 
 * @author jdolan
 */
public class S3SyncTest {

	@Test
	public void quetoo() {
		
		final File destination = new File(FileUtils.getTempDirectory(), "quetoo");

		System.out.println("Sync quetoo to " + destination);

		FileUtils.deleteQuietly(destination);
		
		final S3Sync quetoo = new S3Sync.Builder()
				.withHttpClient(HttpClients.createDefault())
				.withBucketName("quetoo")
				.withPredicate(s -> s.getKey().startsWith("x86_64-apple-darwin"))
				.withMapper(s -> new File(s.getKey().replace("x86_64-apple-darwin", "")))
				.withDestination(destination)
				.build();
		
		quetoo.index()
			.doOnNext(index -> {})
			.flatMapSingle(quetoo::delta)
			.doOnNext(delta -> {})
			.flatMap(quetoo::sync)
			.doOnNext(System.out::println)
			.test()
			.assertNoErrors()
			.assertComplete();
	}

	@Test
	public void quetooData() {
		
		final File destination = new File(FileUtils.getTempDirectory(), "quetoo-data");
		
		FileUtils.deleteQuietly(destination);
		
		System.out.println("Delta quetoo-data to " + destination);
		
		final S3Sync quetooData = new S3Sync.Builder()
				.withHttpClient(HttpClients.createDefault())
				.withBucketName("quetoo-data")
				.withMapper(s -> new File(s.getKey()))
				.withDestination(destination)
				.build();
		
		final AtomicInteger indexCount = new AtomicInteger();
		final AtomicInteger deltaCount = new AtomicInteger();
		
		quetooData.index()
				.doOnNext(index -> indexCount.addAndGet(index.count()))
				.flatMapSingle(quetooData::delta)
				.doOnNext(delta -> deltaCount.addAndGet(delta.count()))
				.test()
				.assertNoErrors()
				.assertComplete();
		
		assertEquals(indexCount.get(), deltaCount.get());
		
		System.out.println("Index: " + indexCount + " Delta: " + deltaCount);
	}
}
