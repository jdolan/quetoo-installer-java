package org.quetoo.update;

import java.util.ArrayList;
import java.util.List;

import org.quetoo.update.aws.S3BucketSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jdolan
 *
 */
public class Manager {
	
	private final Config config;

	private final List<Sync> syncs;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/**
	 * 
	 * @param config
	 */
	public Manager(final Config config) {
		this.config = config;
		
		syncs = new ArrayList<>();
		
		syncs.add(new S3BucketSync.Builder()
				.withAmazonS3Client(config.getAmazonS3Client())
				.withHttpClient(config.getHttpClient())
				.withBucketName("quetoo")
				.withPredicate(s -> s.getKey().startsWith(config.getArchHostPrefix()))
				.withDestination(config.getDir())
				.build());
		
		syncs.add(new S3BucketSync.Builder()
				.withAmazonS3Client(config.getAmazonS3Client())
				.withHttpClient(config.getHttpClient())
				.withBucketName("quetoo-data")
				.withPredicate(s -> true)
				.withDestination(config.getDir("share"))
				.build());
	}
	
	public void update() {
		
		//syncs.parallelStream().for
		
		
	}
}
