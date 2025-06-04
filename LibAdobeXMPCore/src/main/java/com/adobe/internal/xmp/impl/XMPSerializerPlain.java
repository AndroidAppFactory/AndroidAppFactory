// The Plain XMP format is disabled
//
// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================
//
//package com.adobe.internal.xmp.impl;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;
//
//import com.adobe.internal.xmp.XMPConst;
//import com.adobe.internal.xmp.XMPError;
//import com.adobe.internal.xmp.XMPException;
//import com.adobe.internal.xmp.XMPMetaFactory;
//import com.adobe.internal.xmp.options.PropertyOptions;
//import com.adobe.internal.xmp.options.SerializeOptions;
//import com.adobe.internal.xmp.utils.XMLStreamWriterFactory;
//import com.adobe.internal.xmp.utils.XMLStreamWriterImpl;
//
//
///**
// * Serializes the <code>XMPMeta</code>-object using the Plain RDF serialization format.
// * The output is written to an <code>OutputStream</code>
// * according to the <code>SerializeOptions</code>.
// *
// * @author  Stefan Makswit
// * @version $Revision$
// * @since   07.11.2006
// */
//public class XMPSerializerPlain
//{
//	/** default padding */
//	private static final int DEFAULT_PAD = 2048;
//	/** */
//	private static final String PACKET_HEADER  =
//		"xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"";
//	/** The w/r is missing inbetween */
//	private static final String PACKET_TRAILER = "xpacket end=\"";
//	/** */
//	private static final String PACKET_TRAILER2 = "\"";
//
//	/** the XMLStreamWriter */
//	private XMLStreamWriterImpl writer;
//	/** the metadata object to be serialized. */
//	private XMPMetaImpl xmp;
//	/** the size of one unicode char, for UTF-8 set to 1
//	 *  (Note: only valid for ASCII chars lower than 0x80),
//	 *  set to 2 in case of UTF-16 */
//	private int unicodeSize = 1; // UTF-8
//	/** the padding in the XMP Packet, or the length of the complete packet in
//	 *  case of option <em>exactPacketLength</em>. */
//	private int padding;
//	/** the stored serialisation options */
//	private SerializeOptions options;
//	/** the output stream to serialize to */
//	private CountOutputStream outputStream;
//
//
//	/**
//	 * The actual serialisation.
//	 *
//	 * @param xmp the metadata object to be serialized
//	 * @param out outputStream the output stream to serialize to
//	 * @param options the serialization options
//	 *
//	 * @throws XMPException If case of wrong options or any other serialisaton error.
//	 */
//	public void serialize(XMPMetaImpl xmp, OutputStream out, SerializeOptions options)
//			throws XMPException
//	{
//		try
//		{
//			this.xmp = xmp;
//			this.options = options;
//			this.padding = options.getPadding();
//
//			outputStream = new CountOutputStream(out);
//			// need to add serialize options
//			writer = XMLStreamWriterFactory.create(outputStream, options);
//
//			checkOptionsConsistence();
//
//			// Write the packet header PI.
//			if (!options.getOmitPacketWrapper())
//			{
//				writer.writeProcessingInstruction(PACKET_HEADER);
//			}
//
//			// serializes the whole packet without the ending PI
//			serializeAsPlain();
//			writer.flush();
//
//			// Prepares the packet trailer PI.
//			String tailStr = "";
//			if (!options.getOmitPacketWrapper())
//			{
//				tailStr += PACKET_TRAILER;
//				tailStr += options.getReadOnlyPacket() ? 'r' : 'w';
//				tailStr += PACKET_TRAILER2;
//
//				writer.setEscapeWhitespaces(false);
//
//				// adds padding according to length of tail string (UTF-8, one byte per char)
//				addPadding(tailStr.length() + 4 + // +4 for <? ... ?>
//					options.getBaseIndent() * options.getIndent().length() +
//					options.getNewline().length());
//
//				// writes the tail
//				writer.writeProcessingInstruction(tailStr);
//			}
//			writer.close();
//		}
//		catch (IOException e)
//		{
//			throw new XMPException(e.getMessage(), XMPError.INTERNALFAILURE);
//		}
//	}
//
//
//	/**
//	 * Does the main serialisaton of the metadata object.
//	 * @throws IOException Forwards stream writer exceptions
//	 */
//	private void serializeAsPlain() throws IOException
//	{
//		// Write the pxmp:XMP_Packet start tag, optitional name and version string.
//		writer.writeStartElement("pxmp:XMP_Packet");
//		writeNameAndVersion();
//
//		declareUsedNamespaces (xmp.getRoot());
//
//		// Write all of the properties.
//		for (Iterator it = xmp.getRoot().iterateChildren(); it.hasNext(); )
//		{
//			XMPNode schema = (XMPNode) it.next();
//			serializePlainXMPSchema(schema);
//		}
//
//		// Write the pxmp:XMP_Packet end tag.
//		writer.writeEndElement();
//	}
//
//
//	/**
//	 * Writes the metadata object name, the toolkit version and the rdf namespace.
//	 * @throws IOException Forwards writer exceptions
//	 */
//	private void writeNameAndVersion() throws IOException
//	{
//		if (xmp.getObjectName().length() > 0)
//		{
//			writer.writeAttribute("pxmp:about", xmp.getObjectName());
//		}
//
//		if (!options.getOmitVersionAttribute())
//		{
//			writer.writeAttribute("pxmp:xmptk", XMPMetaFactory.getVersionInfo().getMessage());
//		}
//		else
//		{
//			writer.writeAttribute("pxmp:xmptk", "");
//		}
//		writer.writeNamespace("pxmp", XMPConst.NS_PXMP); // pxmp-namespace first
//	}
//
//
//	/**
//	 * Serializes one xmp schema with all of its properties.
//	 * @param schema the schema to serialize
//	 * @throws IOException Forwards writer exceptions
//	 */
//	private void serializePlainXMPSchema(XMPNode schema) throws IOException
//	{
//		// Write the pxmp:SchemaGroup start tag.
//		writer.writeStartElement(schema.getValue() + "XMP_SchemaGroup");
//
//		// Write each of the schema's actual properties.
//		for (Iterator it = schema.iterateChildren(); it.hasNext();)
//		{
//			XMPNode propNode = (XMPNode) it.next();
//			serializePlainXMPProperty(propNode, options.getBaseIndent() + 1);
//		}
//
//		// Write the pxmp:SchemaGroup end tag.
//		writer.writeEndElement();
//	}
//
//
//
//	/**
//	 * Serialize any property or qualifier recursively in plain XMP format.
//	 *
//	 * @param propNode the current property node
//	 * @param indent the current indent
//	 * @throws IOException Forward output exceptions
//	 */
//	private void serializePlainXMPProperty(XMPNode propNode, int indent) throws IOException
//	{
//		PropertyOptions propOpts = propNode.getOptions();
//		boolean isSimple  = propOpts.isSimple();
//		boolean isCompact = propOpts.isCompact();
//
//		String propName = propNode.getName();
//		if (XMPConst.ARRAY_ITEM_NAME.equals(propName))
//		{
//			// rename "[]" to "item" for array items
//			propName = "item";
//		}
//
//		// Write the property name as the beginning of an element start tag.
//		writer.writeStartElement(propName);
//
//		// Write kind attribute and simple qualifiers.
//		writeKindAttribute(propNode);
//		List remainingQuals = writeSimpleQualifier(propNode);
//
//		// Write value or compact property as attribute or compound properties recursively.
//		if (isSimple)
//		{
//			writer.writeAttribute("value", propNode.getValue());
//		}
//		else if (isCompact)
//		{
//			writeCompactProperty(propNode);
//		}
//		else
//		{
//			// Write the element children for struct fields and array items.
//			for (Iterator it = propNode.iterateChildren(); it.hasNext();)
//			{
//				XMPNode child = (XMPNode) it.next();
//				serializePlainXMPProperty(child, indent + 1);
//			}
//		}
//
//		writeCompoundQualifier(remainingQuals, propOpts, indent);
//
//		// Close the start tag or write an end tag, if needed.
//		writer.writeEndElement();
//	}
//
//
//	/**
//	 * Write kind-attribute for tag if needed.
//	 *
//	 * @param propNode a property or qualifier node.
//	 * @throws IOException Forwards the writer exceptions.
//	 */
//	private void writeKindAttribute(XMPNode propNode) throws IOException
//	{
//		PropertyOptions opts = propNode.getOptions();
//		if (opts.isSimple()  &&  opts.isQualifier())
//		{
//			writer.writeAttribute("kind", "qual");
//		}
//		else if (!opts.isSimple())
//		{
//			String kindStr = opts.isCompact() ? "c-" : "";
//
//			if (opts.isStruct())
//			{
//				kindStr += "struct";
//			}
//			else
//			{
//				if (opts.isArrayAlternate())
//				{
//					kindStr += "alt";
//					if (opts.isArrayAltText())
//					{
//						XMPNodeUtils.normalizeLangArray(propNode);
//					}
//				}
//				else if (opts.isArrayOrdered())
//				{
//					kindStr += "seq";
//				}
//				else
//				{
//					kindStr += "bag";
//				}
//
//			}
//
//			if (opts.isQualifier())
//			{
//				kindStr += "-qual";
//			}
//
//			// write the kind attribute
//			writer.writeAttribute("kind", kindStr);
//		}
//	}
//
//
//	/**
//	 * Writes a compact property as attribute(s)
//	 * @param propNode a property node
//	 * @throws IOException Forwards the writer exceptions.
//	 */
//	private void writeCompactProperty(XMPNode propNode) throws IOException
//	{
//		if (propNode.getOptions().isStruct())
//		{
//			for (Iterator it = propNode.iterateChildren(); it.hasNext();)
//			{
//				XMPNode child = (XMPNode) it.next();
//				assert  !child.getOptions().isCompositeProperty()  &&
//						!child.getOptions().getHasQualifiers();
//				writer.writeAttribute(child.getName(), child.getValue());
//			}
//
//		}
//		else
//		{
//			assert propNode.getOptions().isArray();
//
//			// Pick the separator. Defaults to " ", use "; " if any item
//			// value is empty or has XML whitespace.
//			String separator = " ";
//			boolean whiteSpaceFound = false;
//
//			for (Iterator it = propNode.iterateChildren(); it.hasNext();)
//			{
//				XMPNode child = (XMPNode) it.next();
//				if (child.getValue() == null || child.getValue().length() == 0)
//				{
//					separator = "; ";
//					break;
//				}
//
//				for (int i = 0; i < child.getValue().length(); i++)
//				{
//					char c = child.getValue().charAt(i);
//					if (c <= ' ' && c != 0x09 && c != 0x0A && c != 0x0D)
//					{
//						whiteSpaceFound = true;
//						break;
//					}
//				}
//
//				if (whiteSpaceFound)
//				{
//					separator = "; ";
//					break;
//				}
//			}
//
//			// Catenate the item values and write the value attribute.
//
//			StringBuffer arrayValue = new StringBuffer();
//
//			if (!whiteSpaceFound)
//			{
//				for (Iterator it = propNode.iterateChildren(); it.hasNext();)
//				{
//					XMPNode child = (XMPNode) it.next();
//					assert  !child.getOptions().isCompositeProperty()  &&
//							!child.getOptions().getHasQualifiers();
//					arrayValue.append(child.getValue());
//					if (it.hasNext())
//					{
//						arrayValue.append(separator);
//					}
//				}
//			}
//			else
//			{
//				writer.writeAttribute("sep", "; ");
//
//				for (Iterator it = propNode.iterateChildren(); it.hasNext();)
//				{
//					XMPNode child = (XMPNode) it.next();
//					assert !child.getOptions().isCompositeProperty()  &&
//						!child.hasQualifier();
//
//					String value = propNode.getValue();
//					int colonPos = 0;
//					for (int i = 0; i < value.length(); i++)
//					{
//						char ch = value.charAt(i);
//						if (ch == ';')
//						{
//							arrayValue.append(value.substring(colonPos, i));
//							arrayValue.append(';'); // append double semicolon
//							colonPos = i;
//						}
//					}
//					arrayValue.append(value.substring(colonPos, value.length()));
//
//					if (it.hasNext())
//					{
//						arrayValue.append(separator);
//					}
//				}
//			}
//
//			writer.writeAttribute("value", arrayValue.toString());
//		}
//	}
//
//
//
//
//	/**
//	 * Write the simple qualifiers.<br>
//	 * <em>Note:</em> Simple qualifiers on a compact struct are contained elements, not attributes.
//	 *
//	 * @param propNode a property node
//	 * @return Returns the remaining, compound qualifier.
//	 * @throws IOException Forwards the writer exceptions.
//	 */
//	private List writeSimpleQualifier(XMPNode propNode) throws IOException
//	{
//		PropertyOptions opts = propNode.getOptions();
//
//		List remainingQuals = null;
//		if (!opts.isCompact() && !opts.isStruct())
//		{
//			for (Iterator it = propNode.iterateQualifier(); it.hasNext();)
//			{
//				XMPNode qualifier = (XMPNode) it.next();
//				if (!qualifier.getOptions().isCompositeProperty()  &&
//					!qualifier.getOptions().getHasQualifiers())
//				{
//					writer.writeAttribute(qualifier.getName(), qualifier.getValue());
//				}
//				else
//				{
//					if (remainingQuals == null)
//					{
//						remainingQuals = new ArrayList();
//					}
//					remainingQuals.add(qualifier);
//				}
//			}
//		}
//		return remainingQuals;
//	}
//
//
//	/**
//	 * Write the compound qualifiers. Sort them if the property is a regular
//	 * (non-compact) array. It is OK to sort all of the qualifiers. We've
//	 * already output the simple ones, so their order is fixed.
//	 * <p>
//	 * <em>Note:</em> Don't sort the real qualifier vector, sort a copy. Need
//	 * to keep "xml:lang" and "rdf:type" in front!
//	 * <p>
//	 * <em>Note:</em> This includes the simple qualifiers for a compact
//	 * struct.
//	 *
//	 * @param remainingQuals a list of remaining qualifiers that could not be written as attribute.
//	 * @param propOpts the parent property options
//	 * @param indent the current indent
//	 * @throws IOException Forwards the writer exceptions.
//	 */
//	private void writeCompoundQualifier(List remainingQuals, PropertyOptions propOpts, int indent)
//			throws IOException
//	{
//
//		if (remainingQuals != null)
//		{
//			if (!propOpts.isArray()  &&  !propOpts.isCompact())
//			{
//				Collections.sort(remainingQuals);
//			}
//
//			for (Iterator it = remainingQuals.iterator(); it.hasNext();)
//			{
//				XMPNode qualifier = (XMPNode) it.next();
//				serializePlainXMPProperty(qualifier, indent + 1);
//
//// <#AdobePrivate>
//// if ((isCompact  &&  isStruct)  ||
////					qualifier.getOptions().isCompositeProperty()  ||
////				    qualifier.getOptions().hasQualifiers())
//// </#AdobePrivate>
//			}
//
//		}
//	}
//
//	/**
//	 * Checks if the supplied options are consistent.
//	 * @throws XMPException Thrown if options are conflicting
//	 */
//	protected void checkOptionsConsistence() throws XMPException
//	{
//		if (options.getEncodeUTF16BE() | options.getEncodeUTF16LE())
//		{
//			unicodeSize = 2;
//		}
//
//		if (options.getExactPacketLength())
//		{
//			if (options.getOmitPacketWrapper() | options.getIncludeThumbnailPad())
//			{
//				throw new XMPException("Inconsistent options for exact size serialize",
//						XMPError.BADOPTIONS);
//			}
//			if ((options.getPadding() & (unicodeSize - 1)) != 0)
//			{
//				throw new XMPException("Exact size must be a multiple of the Unicode element",
//						XMPError.BADOPTIONS);
//			}
//		}
//		else if (options.getReadOnlyPacket())
//		{
//			if (options.getOmitPacketWrapper() | options.getIncludeThumbnailPad())
//			{
//				throw new XMPException("Inconsistent options for read-only packet",
//						XMPError.BADOPTIONS);
//			}
//			padding = 0;
//		}
//		else if (options.getOmitPacketWrapper())
//		{
//			if (options.getIncludeThumbnailPad())
//			{
//				throw new XMPException("Inconsistent options for non-packet serialize",
//						XMPError.BADOPTIONS);
//			}
//			padding = 0;
//		}
//		else
//		{
//			if (padding == 0)
//			{
//				padding = DEFAULT_PAD * unicodeSize;
//			}
//
//			if (options.getIncludeThumbnailPad())
//			{
//				if (!xmp.doesPropertyExist(XMPConst.NS_XMP, "Thumbnails"))
//				{
//					padding += 10000 * unicodeSize;
//				}
//			}
//		}
//	}
//
//
//	/**
//	 * Writes all used namespaces of the subtree in node to the output.
//	 * The subtree is recursivly traversed.
//	 * @param node the root node of the subtree
//	 * @throws IOException Forwards all writer exceptions.
//	 */
//	private void declareUsedNamespaces(XMPNode node)
//			throws IOException
//	{
//		if (node.getOptions().isSchemaNode())
//		{
//			// The schema node name is the URI, the value is the prefix.
//			String prefix = node.getValue().substring(0, node.getValue().length() - 1);
//			String namespace = node.getName();
////			writeIndent(1);
//			writer.writeNamespace(prefix, namespace);
//		}
//		// not the root node and direct schema children
//		else if (node.getParent() != null  &&
//				 !node.getParent().getOptions().isSchemaNode())
//		{
//			QName qname = new QName(node.getName());
//			String ns = XMPMetaFactory.getSchemaRegistry().getNamespaceURI(qname.getPrefix());
//			writer.writeNamespace(qname.getPrefix(), ns);
//		}
//
//		// iterate children except array items
//		if (!node.getOptions().isArray())
//		{
//			for (Iterator it = node.iterateChildren(); it.hasNext();)
//			{
//				XMPNode child = (XMPNode) it.next();
//				declareUsedNamespaces(child);
//			}
//		}
//
//		for (Iterator it = node.iterateQualifier(); it.hasNext();)
//		{
//			XMPNode qualifier = (XMPNode) it.next();
//			declareUsedNamespaces(qualifier);
//		}
//	}
//
//
//	/**
//	 * Calulates the padding according to the options and write it to the stream.
//	 * @param tailLength the length of the tail string
//	 * @throws XMPException thrown if packet size is to small to fit the padding
//	 * @throws IOException forwards writer errors
//	 * @throws IOException
//	 */
//	private void addPadding(int tailLength) throws XMPException, IOException
//	{
//		if (options.getExactPacketLength())
//		{
//			// the string length is equal to the length of the UTF-8 encoding
//			int minSize = outputStream.getBytesWritten() + tailLength * unicodeSize;
//			if (minSize > padding)
//			{
//				throw new XMPException("Can't fit into specified packet size",
//					XMPError.BADSERIALIZE);
//			}
//			padding -= minSize;	// Now the actual amount of padding to add.
//		}
//
//		// fix rest of the padding according to Unicode unit size.
//		padding /= unicodeSize;
//
//		int newlineLen = options.getNewline().length();
//		if (padding >= newlineLen)
//		{
//			padding -= newlineLen;	// Write this newline last.
//			while (padding >= (100 + newlineLen))
//			{
//				writeNewline();
//				writeChars(100, ' ');
//				padding -= (100 + newlineLen);
//			}
//			writeNewline();
//			writeChars(padding, ' ');
//		}
//		else
//		{
//			writeChars(padding, ' ');
//		}
//	}
//
//
//	/**
//	 * Writes a newline according to the options.
//	 * @throws IOException Forwards exception
//	 */
//	private void writeNewline() throws IOException
//	{
//		writer.writeCharacters(options.getNewline());
//	}
//
//
//	/**
//	 * Writes an amount of chars, mostly spaces
//	 * @param number number of chars
//	 * @param c a char
//	 * @throws IOException
//	 */
//	private void writeChars(int number, char c) throws IOException
//	{
//		char[] buf = new char[number];
//		Arrays.fill(buf, c);
//		writer.writeCharacters(buf, 0, number);
//	}
//}