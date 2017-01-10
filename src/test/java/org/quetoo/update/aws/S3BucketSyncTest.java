package org.quetoo.update.aws;

import static com.amazonaws.regions.Regions.US_WEST_2;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.quetoo.update.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * Integration tests for the {@link S3BucketSync} class.
 * 
 * @author jdolan
 */
public class S3BucketSyncTest implements Sync.Listener {
		
	private AmazonS3Client amazonS3Client;
	
	private CloseableHttpClient httpClient;
	
	private S3BucketSync s3BucketSync;
		
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Before
	public void before() {
		
		final String tmp = FileUtils.getTempDirectoryPath();

		final File destination = new File(FilenameUtils.concat(tmp, "radiantjs"));
		log.info("Syncing to desination {}", destination);
				
		amazonS3Client = new AmazonS3Client();
		amazonS3Client.setRegion(Region.getRegion(US_WEST_2));
		
		httpClient = HttpClients.createDefault();
		
		s3BucketSync = new S3BucketSync.Builder()
				.withAmazonS3Client(new AmazonS3Client())
				.withHttpClient(httpClient)
				.withBucketName("radiantjs")
				.withDestination(destination)
				.withListener(this)
				.build();
		
		FileUtils.deleteQuietly(destination);
	}
	
	@Override
	public void onCountObjects(int count) {
		log.debug("onCountObjects {}", count);
	}

	@Override
	public void onSyncObject(String name) {
		log.debug("onSyncObject {}", name);
	}

	@Override
	public void onRemoveFile(File file) {
		log.debug("onRemoveFile {}", file);
	}

	@Test
	public void sync() throws IOException {
		
		Set<File> files = s3BucketSync.sync();
		
		assertNotNull(files);
		assertFalse(files.isEmpty());
	}
}
