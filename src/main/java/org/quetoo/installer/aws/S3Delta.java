package org.quetoo.installer.aws;

import java.util.Iterator;
import java.util.List;

import org.quetoo.installer.Asset;
import org.quetoo.installer.Delta;
import org.quetoo.installer.Index;

/**
 * An abstraction for the {@link Delta} contents of an {@link S3Bucket}.
 * 
 * @author jdolan
 */
public class S3Delta implements Delta {

	private final S3Bucket bucket;
	private final List<S3Object> objects;

	/**
	 * Instantiates an {@link S3Delta} with the given {@link S3Bucket} and {@link S3Object}s.
	 * 
	 * @param bucket The {@link S3Bucket}.
	 * @param objects The delta {@link S3Object}s.
	 */
	public S3Delta(final S3Bucket bucket, final List<S3Object> objects) {
		this.bucket = bucket;
		this.objects = objects;
	}

	@Override
	public Iterator<Asset> iterator() {
		return objects.stream().map(obj -> (Asset) obj).iterator();
	}
	
	@Override
	public int count() {
		return getObjects().size();
	}

	@Override
	public long size() {
		return getObjects().stream().mapToLong(asset -> asset.size()).sum();
	}

	@Override
	public Index getIndex() {
		return getBucket();
	}

	public S3Bucket getBucket() {
		return bucket;
	}

	public List<S3Object> getObjects() {
		return objects;
	}
}
