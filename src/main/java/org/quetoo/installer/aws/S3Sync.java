package org.quetoo.installer.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.quetoo.installer.Asset;
import org.quetoo.installer.Delta;
import org.quetoo.installer.Index;
import org.quetoo.installer.Sync;

import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Synchronizes a local file system destination with an S3 bucket.
 * 
 * @author jdolan
 */
public class S3Sync implements Sync {

	/**
	 * A builder for creating {@link S3Sync} instances.
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
			this.mapper = mapper;
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

		public S3Sync build() {
			return new S3Sync(this);
		}
	}

	private final CloseableHttpClient httpClient;
	private final String bucketName;
	private final Predicate<S3Object> predicate;
	private final Function<S3Object, File> mapper;
	private final File destination;

	/**
	 * Instantiates an {@link S3Sync} with the given Builder.
	 * 
	 * @param builder The Builder.
	 */
	private S3Sync(final Builder builder) {

		httpClient = builder.httpClient;
		bucketName = builder.bucketName;
		predicate = builder.predicate;
		mapper = builder.mapper;
		destination = builder.destination;
	}

	/**
	 * A specialized ResponseHandler for conveniently dealing with response
	 * InputStreams.
	 * 
	 * @param <T> The parsed response object.
	 */
	private interface ResponseHandler<T> {
		T handleResponse(final InputStream inputStream) throws IOException;
	}

	/**
	 * Executes an HTTP GET request for the specified path.
	 * 
	 * @param path    The path.
	 * @param query   The query string, or null.
	 * @param handler The response handler.
	 * @return The parsed response.
	 * @throws IOException If an error occurs.
	 */
	private <T> T executeHttpRequest(final String path, final Map<String, String> params,
			final ResponseHandler<T> handler) throws IOException {

		final URIBuilder uri = new URIBuilder()
				.setScheme("https")
				.setHost(bucketName + ".s3.amazonaws.com/")
				.setPath(path);

		params.forEach((param, value) -> uri.setParameter(param, value));

		try {
			return httpClient.execute(new HttpGet(uri.build()), res -> {
				return handler.handleResponse(res.getEntity().getContent());
			});
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Performs a delta check for the given {@link S3Object}.
	 * 
	 * @param obj The {@link S3Object}.
	 * @return True if the object represents a delta, false otherwise.
	 * @throws IOException If an error occurs.
	 */
	private boolean delta(final S3Object obj) throws IOException {

		final File file = map(obj);
		if (file.exists()) {
			if (file.isDirectory() && obj.isDirectory()) {
				return false;
			}

			assert (file.isFile());

			final String md5 = DigestUtils.md5Hex(FileUtils.readFileToByteArray(file));
			if (StringUtils.equals(md5, obj.getEtag())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Synchronizes the given {@link S3Object}.
	 * 
	 * @param obj The {@link S3Object}.
	 * @return The resulting File.
	 * @throws IOException If an error occurs.
	 */
	private File sync(final S3Object obj) throws IOException {

		final File file = map(obj);
		if (file.exists()) {
			if (file.isDirectory() != obj.isDirectory()) {
				FileUtils.deleteQuietly(file);
			}
		}

		if (obj.isDirectory()) {
			FileUtils.forceMkdir(file);
		} else {
			FileUtils.forceMkdirParent(file);
			executeHttpRequest(obj.getKey(), Collections.emptyMap(), inputStream -> {
				return IOUtils.copy(inputStream, new FileOutputStream(file));
			});
		}

		return file;
	}

	@Override
	public File map(final Asset asset) {
		return new File(destination, mapper.apply((S3Object) asset).getPath());
	}

	@Override
	public Observable<Index> index() {
		return Observable.create(source -> {

			final Map<String, String> params = new HashMap<>();
			while (true) {

				final S3Bucket bucket = new S3Bucket(this, executeHttpRequest("", params, S3::getDocument));

				if (predicate != null) {
					source.onNext(bucket.filter(predicate));
				} else {
					source.onNext(bucket);
				}

				if (bucket.count() < 1000) {
					source.onComplete();
					break;
				}

				params.put("marker", bucket.getMarker());
			}
		});
	}

	@Override
	public Single<Delta> delta(final Index index) {
		return Single.just(index)
				.cast(S3Bucket.class)
				.flatMap(bucket -> {
					return Observable.fromIterable(bucket)
							.cast(S3Object.class)
							.filter(this::delta)
							.toList()
							.map(objects -> new S3Delta(bucket, objects));
				});
	}

	@Override
	public Observable<File> sync(final Delta delta) {
		return Observable.fromIterable(delta).map(asset -> (S3Object) asset).map(this::sync);
	}

	@Override
	public void close() throws IOException {
		httpClient.close();
	}
}
