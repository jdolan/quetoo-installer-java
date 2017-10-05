package org.quetoo.installer.aws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.quetoo.installer.Delta;
import org.quetoo.installer.Index;

/**
 * Integration tests for the {@link S3Sync} class.
 * 
 * @author jdolan
 */
public class S3SyncTest {

	private final File destination = new File(FileUtils.getTempDirectory(), "quetoo");

	private final S3Sync sync = new S3Sync.Builder()
			.withHttpClient(HttpClients.createDefault())
			.withBucketName("quetoo")
			.withPredicate(s -> s.getKey().contains("x86_64-apple-darwin"))
			.withMapper(s -> new File(s.getKey().replace("x86_64-apple-darwin", "")))
			.withDestination(destination)
			.build();

	@Before
	public void before() {
		FileUtils.deleteQuietly(destination);
	}

	@Test
	public void test() {

		System.out.println("Syncing to desination " + destination);

		final Index index = sync.index().blockingGet();

		assertNotNull(index);
		assertNotNull(index.iterator());

		assertEquals(sync, index.getSync());

		// index.forEach(System.out::println);

		final Delta delta = sync.delta(index).blockingGet();

		assertNotNull(delta);
		assertNotNull(delta.iterator());

		assertEquals(sync, delta.getIndex().getSync());

		 delta.forEach(System.out::println);

		final List<File> files = sync.sync(delta).toList().blockingGet();

		assertNotNull(files);
		assertEquals(delta.count(), files.size());
	}
}
