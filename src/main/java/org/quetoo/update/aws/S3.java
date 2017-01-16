package org.quetoo.update.aws;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Static utilities for interacting with S3.
 * 
 * @author jdolan
 */
public class S3 {

	/**
	 * A convenience method for instantiating DocumentBuilder.
	 * 
	 * @return A DocumentBuilder suitable for parsing S3 XML.
	 * 
	 * @throws IOException If an error occurs.
	 */
	public static DocumentBuilder getDocumentBuilder() throws IOException {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			throw new IOException(pce);
		}
	}

	/**
	 * A convenience method for parsing S3 XML documents.
	 * 
	 * @param inputStream The InputStream to parse.
	 * 
	 * @return The parsed Document.
	 * 
	 * @throws IOException If an error occurs.
	 */
	public static Document getDocument(final InputStream inputStream) throws IOException {
		try {
			return getDocumentBuilder().parse(inputStream);
		} catch (SAXException saxe) {
			throw new IOException(saxe);
		}
	}

	/**
	 * A convenience method for resolving required child nodes.
	 * 
	 * @param node The node from which to resolve the child.
	 * @param name The child node's local name.
	 * 
	 * @return The child node.
	 */
	public static Node getChildNode(final Node node, final String name) {
		return ((Element) node).getElementsByTagName(name).item(0);
	}

	/**
	 * Parses a String from the named child of `node`.
	 * 
	 * @param node The node from which to resolve the child.
	 * @param name The child node's local name.
	 * 
	 * @return The String representation of the child node.
	 */
	public static String getString(final Node node, final String name) {
		return getChildNode(node, name).getChildNodes().item(0).getNodeValue();
	}

	/**
	 * Parses an Instant from the named child of `node`.
	 * 
	 * @param node The node from which to resolve the child.
	 * @param name The child node's local name.
	 * 
	 * @return The Instant representation of the parsed ISO date and time.
	 */
	public static Instant getInstant(final Node node, final String name) {
		return Instant.from(ISO_DATE_TIME.parse(getString(node, name)));
	}

	/**
	 * A convenience method for iterating child nodes.
	 * 
	 * @param node The node from which to resolve children.
	 * @param name The children nodes' local name.
	 * 
	 * @return A Stream of child Nodes.
	 */
	public static Stream<Node> getChildNodes(final Node node, final String name) {

		final NodeList list = ((Element) node).getElementsByTagName(name);
		final Node nodes[] = new Node[list.getLength()];

		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = list.item(i);
		}

		return Stream.of(nodes);
	}
}
