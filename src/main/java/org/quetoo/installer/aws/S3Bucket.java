package org.quetoo.installer.aws;

import static org.quetoo.installer.aws.S3.getChildNodes;
import static org.quetoo.installer.aws.S3.getString;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.quetoo.installer.Asset;
import org.quetoo.installer.Index;
import org.quetoo.installer.Sync;
import org.w3c.dom.Document;

/**
 * An abstraction for the parsed XML contents of an AWS S3 bucket.
 * 
 * @author jdolan
 */
public class S3Bucket implements Index {

	private static final String NAME = "Name";
	private static final String CONTENTS = "Contents";

	private final S3Sync sync;
	private final String name;
	private final List<S3Object> objects;

	/**
	 * Instantiates a new {@link S3Bucket} from the given XML document.
	 * 
	 * @param sync The {@link S3Sync}.
	 * @param doc A parsed S3 bucket listing (e.g. `http://quetoo.s3.amazonaws.com/`).
	 */
	public S3Bucket(final S3Sync sync, final Document doc) {
		this.sync = sync;

		name = getString(doc.getDocumentElement(), NAME);

		objects = getChildNodes(doc.getDocumentElement(), CONTENTS)
				.map(node -> new S3Object(this, node))
				.collect(Collectors.toList());
	}

	/**
	 * Filters this bucket with the given predicate.
	 * 
	 * @param predicate The Predicate.
	 * @return The {@link S3Bucket}.
	 */
	public S3Bucket filter(final Predicate<S3Object> predicate) {
		objects.removeIf(predicate.negate());
		return this;
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
	public Sync getSync() {
		return getS3Sync();
	}

	public S3Sync getS3Sync() {
		return sync;
	}

	public String getName() {
		return name;
	}

	public List<S3Object> getObjects() {
		return objects;
	}
}
