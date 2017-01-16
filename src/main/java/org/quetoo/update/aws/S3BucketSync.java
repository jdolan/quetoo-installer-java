package org.quetoo.update.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.quetoo.update.Sync;

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

		private CloseableHttpClient httpClient;
		private String bucketName;
		private Predicate<S3Object> predicate;
		private Function<S3Object, File> mapper;
		private File destination;
		
		public Builder withHttpClient(final CloseableHttpClient httpClient) {
			this.httpClient = httpClient;
			return this;
		}

		public Builder withBucketName(final String bucketName) {
			this.bucketName = bucketName;
			return this;
		}
		
		public Builder withPredicate(final Predicate<S3Object> predicate) {
			this.predicate = predicate;
			return this;
		}
		
		public Builder withMapper(final Function<S3Object, File> mapper) {
			this.mapper= mapper;
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

	private final CloseableHttpClient httpClient;
	private final String bucketName;
	private final Predicate<S3Object> predicate;
	private final Function<S3Object, File> mapper;
	private final File destination;

	/**
	 * Instantiates an {@link S3BucketSync} with the given Builder.
	 * 
	 * @param builder The Builder.
	 */
	private S3BucketSync(final Builder builder) {

		httpClient = builder.httpClient;
		bucketName = builder.bucketName;
		predicate = builder.predicate;
		mapper = builder.mapper;
		destination = builder.destination;
	}
	
	/**
	 * A specialized ResponseHandler for conveniently dealing with response InputStreams.
	 * 
	 * @param <T> The parsed response object.
	 */
	private interface ResponseHandler<T> {
		T handleResponse(final InputStream inputStream) throws IOException;
	}
	
	/**
	 * Executes an HTTP GET request for the specified path.
	 * 
	 * @param path The path.
	 * @param handler The response handler.
	 * 
	 * @return The parsed response.
	 * 
	 * @throws IOException If an error occurs.
	 */
	private <T> T executeHttpRequest(final String path, ResponseHandler<T> handler) throws IOException {
		
		final String uri = "http://" + bucketName + ".s3.amazonaws.com/" + (path != null ? path : "");
		
		return httpClient.execute(new HttpGet(uri), res -> {
			return handler.handleResponse(res.getEntity().getContent());
		});
	}

	/**
	 * Syncs the specified {@link S3Object} to the configured destination.
	 * 
	 * @param obj The object to sync.
	 * 
	 * @return The resulting File.
	 * 
	 * @throws IOException If an error occurs.
	 */
	private File sync(final S3Object obj) throws IOException {

		final boolean wantsDirectory = obj.getKey().endsWith("/");

		final File file = new File(destination, mapper.apply(obj).getPath());
		
		if (file.exists()) {
			final boolean isDirectory = file.isDirectory();
			
			if (isDirectory != wantsDirectory) {
				FileUtils.deleteQuietly(file);
			}
		}
						
		if (!file.exists() || file.lastModified() < obj.getLastModifiedTime()) {
			
			if (wantsDirectory) {
				FileUtils.forceMkdir(file);
			} else {
				FileUtils.forceMkdirParent(file);
				
				executeHttpRequest(obj.getKey(), inputStream -> {
					return IOUtils.copy(inputStream, new FileOutputStream(file));
				});
			}
		}
		
		return file;
	}

	@Override
	public Observable<File> sync() throws IOException {

		FileUtils.forceMkdir(destination);
		
		final S3Bucket bucket = new S3Bucket(executeHttpRequest(null, S3::getDocument));

		return Observable.fromIterable(bucket).filter(predicate).map(this::sync);
	}
}
