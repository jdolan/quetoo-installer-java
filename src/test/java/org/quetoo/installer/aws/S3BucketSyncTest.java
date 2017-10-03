package org.quetoo.installer.aws;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.quetoo.installer.Asset;

/**
 * Integration tests for the {@link S3BucketSync} class.
 * 
 * @author jdolan
 */
public class S3BucketSyncTest {
		
	private S3BucketSync s3BucketSync;
		
	@Before
	public void before() {
		
		final File destination = new File(FileUtils.getTempDirectory(), "quetoo");
		
		System.out.println("Syncing to desination " + destination);
		
		s3BucketSync = new S3BucketSync.Builder()
				.withHttpClient(HttpClients.createDefault())
				.withBucketName("quetoo")
				.withPredicate(s -> s.getKey().startsWith("x86_64-apple-darwin"))
				.withMapper(s -> new File(s.getKey().replace("x86_64-apple-darwin", "")))
				.withDestination(destination)
				.build();
		
		FileUtils.deleteQuietly(destination);
	}

	@Test
	public void sync() throws IOException {
		
		final List<Asset> delta = s3BucketSync.delta()
				.doOnNext(System.out::println)
				.toList()
				.blockingGet();
		
		assertNotNull(delta);
		
		final List<File> files = s3BucketSync.sync()
				.doOnNext(System.out::println)
				.toList()
				.blockingGet();

		assertNotNull(files);
	}
}
