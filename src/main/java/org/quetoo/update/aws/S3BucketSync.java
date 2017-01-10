package org.quetoo.update.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

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
		private File destination;
		private Listener listener;

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

		public Builder withDestination(final File destination) {
			this.destination = destination;
			return this;
		}

		public Builder withDestination(final String destination) {
			this.destination = new File(destination);
			return this;
		}

		public Builder withListener(final Listener listener) {
			this.listener = listener;
			return this;
		}

		public S3BucketSync build() {
			return new S3BucketSync(this);
		}
	}

	private final AmazonS3Client amazonS3Client;
	private final CloseableHttpClient httpClient;
	private final String bucketName;
	private final File destination;
	private final Listener listener;

	/**
	 * Instantiates an {@link S3BucketSync} with the given Builder.
	 * 
	 * @param builder The Builder.
	 */
	private S3BucketSync(@NotNull final Builder builder) {

		this.amazonS3Client = builder.amazonS3Client;
		this.httpClient = builder.httpClient;
		this.bucketName = builder.bucketName;
		this.destination = builder.destination;
		this.listener = builder.listener;
	}

	/**
	 * Synchronizes the File represented in `s` to the destination.
	 * 
	 * @param s The summary of the object to synchronize.
	 * @return The resulting File.
	 */
	protected File syncObject(final S3ObjectSummary s) {

		final String path = FilenameUtils.separatorsToSystem(s.getKey());
		final String absPath = FilenameUtils.concat(destination.getAbsolutePath(), path);

		final File file = new File(absPath);

		final boolean wantsDirectory = s.getKey().endsWith("/");

		if (file.exists()) {

			final boolean isDirectory = file.isDirectory();

			if (isDirectory != wantsDirectory) {

				if (listener != null) {
					listener.onRemoveFile(file);
				}

				FileUtils.deleteQuietly(file);
			}
		}

		if (listener != null) {
			listener.onSyncObject(s.getKey());
		}

		if (!file.exists() || file.lastModified() < s.getLastModified().getTime()) {
			try {
				if (wantsDirectory) {
					FileUtils.forceMkdir(file);
				} else {
					FileUtils.forceMkdirParent(file);

					final String uri = amazonS3Client.getUrl(bucketName, s.getKey()).toString();

					try (CloseableHttpResponse res = httpClient.execute(new HttpGet(uri))) {
						res.getEntity().writeTo(new FileOutputStream(file));
					}
				}
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}

		return file;
	}

	/**
	 * Prunes the destination directory to contain only files found in `entries`.
	 * 
	 * @param entries The newly synchronized destination contents.
	 */
	protected Set<File> prune(final Set<File> entries) {

		if (entries != null) {
			for (File file : FileUtils.listFiles(destination, null, true)) {
				if (!entries.contains(file)) {

					if (listener != null) {
						listener.onRemoveFile(file);
					}

					FileUtils.deleteQuietly(file);
				}
			}
		}

		return entries;
	}

	@Override
	public Set<File> sync() throws IOException {

		FileUtils.forceMkdir(destination);

		List<S3ObjectSummary> summaries = new ArrayList<>();

		ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName);
		ListObjectsV2Result res;
		do {
			res = amazonS3Client.listObjectsV2(req);
			summaries.addAll(res.getObjectSummaries());
			req.setContinuationToken(res.getContinuationToken());
		} while (res.isTruncated());

		if (listener != null) {
			listener.onCountObjects(summaries.size());
		}

		final Set<File> entries = summaries.parallelStream().map(this::syncObject)
				.collect(Collectors.toSet());

		return prune(entries);
	}
}
