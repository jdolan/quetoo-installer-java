package org.quetoo.update.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.quetoo.update.Sync;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.sun.istack.internal.NotNull;

import io.reactivex.Observable;
import io.reactivex.functions.Predicate;


/**
 * Synchronizes a local file system destination with an S3 bucket.
 * 
 * @author jdolan
 */
public class S3BucketSync implements Sync {

	/**
	 * A builder for creating {@link S3BucketSync} instances.
	 */
	public static class Builder {

		private AmazonS3Client amazonS3Client;
		private CloseableHttpClient httpClient;
		private String bucketName;
		private Predicate<S3ObjectSummary> predicate;
		private File destination;

		public Builder withAmazonS3Client(final AmazonS3Client amazonS3Client) {
			this.amazonS3Client = amazonS3Client;
			return this;
		}

		public Builder withHttpClient(final CloseableHttpClient httpClient) {
			this.httpClient = httpClient;
			return this;
		}

		public Builder withBucketName(final String bucketName) {
			this.bucketName = bucketName;
			return this;
		}
		
		public Builder withPredicate(final Predicate<S3ObjectSummary> predicate) {
			this.predicate = predicate;
			return this;
		}

		public Builder withDestination(final File destination) {
			this.destination = destination;
			return this;
		}

		public Builder withDestination(final String destination) {
			this.destination = new File(destination);
			return this;
		}

		public S3BucketSync build() {
			return new S3BucketSync(this);
		}
	}

	private final AmazonS3Client amazonS3Client;
	private final CloseableHttpClient httpClient;
	private final String bucketName;
	private final Predicate<S3ObjectSummary> predicate;
	private final File destination;

	/**
	 * Instantiates an {@link S3BucketSync} with the given Builder.
	 * 
	 * @param builder The Builder.
	 */
	private S3BucketSync(@NotNull final Builder builder) {

		this.amazonS3Client = builder.amazonS3Client;
		this.httpClient = builder.httpClient;
		this.bucketName = builder.bucketName;
		this.predicate = builder.predicate;
		this.destination = builder.destination;
	}

	private File sync(final S3ObjectSummary summary) throws IOException {

		final String path = FilenameUtils.separatorsToSystem(summary.getKey());
		final String absPath = FilenameUtils.concat(destination.getAbsolutePath(), path);

		final File file = new File(absPath);
		
		final boolean wantsDirectory = summary.getKey().endsWith("/");

		if (file.exists()) {
			final boolean isDirectory = file.isDirectory();
			
			if (isDirectory != wantsDirectory) {
				FileUtils.deleteQuietly(file);
			}
		}
						
		if (!file.exists() || file.lastModified() < summary.getLastModified().getTime()) {
			
			if (wantsDirectory) {
				FileUtils.forceMkdir(file);
			} else {
				FileUtils.forceMkdirParent(file);
				
				final String uri = amazonS3Client.getUrl(bucketName, summary.getKey()).toString();

				try (CloseableHttpResponse res = httpClient.execute(new HttpGet(uri))) {
					res.getEntity().writeTo(new FileOutputStream(file));
				}
			}
		}
		
		return file;
	}

	@Override
	public Observable<File> sync() throws IOException {

		FileUtils.forceMkdir(destination);

		List<S3ObjectSummary> summaries = new ArrayList<>();

		ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName);
		ListObjectsV2Result res;
		do {
			res = amazonS3Client.listObjectsV2(req);
			summaries.addAll(res.getObjectSummaries());
			req.setContinuationToken(res.getContinuationToken());
		} while (res.isTruncated());

		return Observable.fromIterable(summaries).filter(predicate).map(this::sync);
	}
}
