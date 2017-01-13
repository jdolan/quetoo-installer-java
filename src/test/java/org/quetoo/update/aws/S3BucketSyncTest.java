package org.quetoo.update.aws;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import io.reactivex.Observable;

/**
 * Integration tests for the {@link S3BucketSync} class.
 * 
 * @author jdolan
 */
public class S3BucketSyncTest {
		
	private AmazonS3Client amazonS3Client;
	
	private CloseableHttpClient httpClient;
	
	private S3BucketSync s3BucketSync;
		
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Before
	public void before() {
		
		final String tmp = FileUtils.getTempDirectoryPath();

		final File destination = new File(FilenameUtils.concat(tmp, "quetoo"));
		log.info("Syncing to desination {}", destination);
				
		amazonS3Client = new AmazonS3Client();
		amazonS3Client.setRegion(Region.getRegion(Regions.US_EAST_1));
		
		httpClient = HttpClients.createDefault();
		
		s3BucketSync = new S3BucketSync.Builder()
				.withAmazonS3Client(new AmazonS3Client())
				.withHttpClient(httpClient)
				.withBucketName("quetoo")
				.withPredicate(s -> s.getKey().startsWith("x86_64-apple-darwin"))
				.withDestination(destination)
				.build();
		
		FileUtils.deleteQuietly(destination);
	}
	
	private void onNext(final File file) {
		log.debug(file.toString());
	}
	
	private void onError(final Throwable t) {
		log.error(t.getMessage(), t);
		fail();
	}

	@Test
	public void sync() throws IOException {
		
		Observable<File> files = s3BucketSync.sync();
		
		files.subscribe(this::onNext, this::onError);
		files.toList().subscribe(f -> {
			assertNotNull(f);
			assertFalse(f.isEmpty());
		});
	}
}
