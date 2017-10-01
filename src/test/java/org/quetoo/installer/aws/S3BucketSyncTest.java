package org.quetoo.installer.aws;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;

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
		
		List<File> files = s3BucketSync.sync(asset -> {
			System.out.println("Read " + asset);
		}, asset -> {
			System.out.println("Delta " + asset);
		}, asset -> {
			System.out.println("Sync " + asset);
		}).toList().blockingGet();

		assertNotNull(files);
		assertFalse(files.isEmpty());
	}
}
