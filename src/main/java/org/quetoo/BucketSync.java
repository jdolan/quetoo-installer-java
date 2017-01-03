package org.quetoo;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * Synchronizes a local file system destination with an S3 bucket.
 * 
 * @author jdolan
 */
public class BucketSync {

	/**
	 * A listener for observing {@link BucketSync} instances.
	 */
	public interface Listener {

		/**
		 * @param count The count of objects to be synchronized.
		 */
		void onCountObjects(int count);

		/**
		 * @param name The object name to be synchronized.
		 */
		void onSyncObject(String name);

		/**
		 * @param file The file to be removed.
		 */
		void onRemoveFile(File file);
	}

	/**
	 * A builder for creating {@link BucketSync} instances.
	 */
	public static class Builder {

		private AmazonS3Client amazonS3Client;
		private String bucketName;
		private File destination;
		private Listener listener;

		public Builder withAmazonS3Client(final AmazonS3Client amazonS3Client) {
			this.amazonS3Client = amazonS3Client;
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

		public Builder withListener(final Listener listener) {
			this.listener = listener;
			return this;
		}

		public BucketSync build() {
			return new BucketSync(this);
		}
	}

	/**
	 * @return A new Builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	private final AmazonS3Client amazonS3Client;
	private final String bucketName;
	private final File destination;
	private final Listener listener;

	/**
	 * Instantiates a BucketSync with the given Builder.
	 * 
	 * @param builder The Builder.
	 */
	private BucketSync(@NotNull final Builder builder) {

		this.amazonS3Client = builder.amazonS3Client;
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
	protected File sync(final S3ObjectSummary s) {

		if (listener != null) {
			listener.onSyncObject(s.getKey());
		}

		final String path = FilenameUtils.separatorsToSystem(s.getKey());
		final String absPath = FilenameUtils.concat(destination.getAbsolutePath(), path);

		final File file = new File(absPath);

		if (!file.exists() || file.lastModified() < s.getLastModified().getTime()) {
			try {
				FileUtils.forceMkdirParent(file);

				final S3Object obj = amazonS3Client.getObject(s.getBucketName(), s.getKey());

				FileUtils.copyInputStreamToFile(obj.getObjectContent(), file);
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

					file.delete();
				}
			}
		}
		
		return entries;
	}

	/**
	 * Synchronizes this bucket to its configured destination.
	 * 
	 * @return A List of all Files synchronized from the S3 bucket.
	 * @throws IOException If an error occurs.
	 */
	public Set<File> sync() throws IOException {

		FileUtils.forceMkdirParent(destination);

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

		Set<File> entries = summaries.stream().map(s -> {
			return sync(s);
		}).collect(Collectors.toSet());

		return prune(entries);
	}
}
