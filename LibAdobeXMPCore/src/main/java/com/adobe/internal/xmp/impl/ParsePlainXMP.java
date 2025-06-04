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
//import org.w3c.dom.Node;
//
//import com.adobe.internal.xmp.XMPConst;
//import com.adobe.internal.xmp.XMPError;
//import com.adobe.internal.xmp.XMPException;
//import com.adobe.internal.xmp.XMPMetaFactory;
//import com.adobe.internal.xmp.XMPSchemaRegistry;
//import com.adobe.internal.xmp.options.PropertyOptions;
//
//
///**
// * Parser for Plain XML serialisation of RDF.
// *
// * @author Stefan Makswit
// * @version $Revision$
// * @since 14.11.2006
// */
//public class ParsePlainXMP implements XMPConst
//{
//	/** The node might or might not be a qualifier. */
//	private static final int ALLOW_QUALIFIER = 0;
//	/** The node must be a qualifier. */
//	private static final int REQUIRE_QUALIFIER = 1;
//	/** The node must not be a qualifier. */
//	private static final int REQUIRE_NON_QUALIFIER = 2;
//
//
//	/**
//	 * The main parsing method. The XML tree is walked through from the root
//	 * node and and XMP tree is created.
//	 *
//	 * @param xmlRoot
//	 *            the XML root node
//	 * @return Returns an XMP metadata object (not normalized)
//	 * @throws XMPException
//	 *             Occurs if the parsing fails for any reason.
//	 */
//	static XMPMetaImpl parse(Node xmlRoot) throws XMPException
//	{
//		XMPMetaImpl xmp = new XMPMetaImpl();
//
//		// handle about-attribute
//		Node about = xmlRoot.getAttributes().getNamedItemNS(NS_PXMP, "about");
//		if (about != null)
//		{
//			xmp.setObjectName(about.getNodeValue());
//		}
//
//		// Process the SchemaGroup elements and top level properties.
//		for (int i = 0; i < xmlRoot.getChildNodes().getLength(); i++)
//		{
//			Node pxmpSchema = xmlRoot.getChildNodes().item(i);
//			if (pxmpSchema.getNodeType() == Node.ELEMENT_NODE)
//			{
//				if ("XMP_SchemaGroup".equals(pxmpSchema.getLocalName()))
//				{
//					String namespace = pxmpSchema.getNamespaceURI();
//					if (NS_DC_DEPRECATED.equals(namespace))
//					{
//						// Fix a legacy DC namespace
//						// FfF: remove it after CS3
//						namespace = NS_DC;
//					}
//
//					XMPNode xmpSchema = XMPNodeUtils.findSchemaNode(xmp.getRoot(), namespace,
//						pxmpSchema.getPrefix(), true);
//					xmpSchema.setImplicit(false);
//
//					parseSchema(pxmpSchema, xmpSchema);
//				}
//				else
//				{
//					throw new XMPException(
//							"Children of pxmp:XMP_Packet must be groupNS:XMP_SchemaGroup elements",
//							XMPError.BADXMP);
//				}
//			}
//		}
//
//		return xmp;
//	}
//
//
//
//	/**
//	 * Parse one schema.
//	 *
//	 * @param pxmpSchema the XML schema node
//	 * @param xmpSchema the xmp schema node
//	 * @throws XMPException Quites the parsing with a fatal error.
//	 */
//	private static void parseSchema(Node pxmpSchema, XMPNode xmpSchema)
//			throws XMPException
//	{
//		// iterate recursively through the properties
//		for (int j = 0; j < pxmpSchema.getChildNodes().getLength(); j++)
//		{
//			Node pxmpProp = pxmpSchema.getChildNodes().item(j);
//			if (pxmpProp.getNodeType() == Node.ELEMENT_NODE)
//			{
//				if (pxmpSchema.getNamespaceURI().equals(pxmpProp.getNamespaceURI()))
//				{
//					addXMPNode(xmpSchema, pxmpProp, REQUIRE_NON_QUALIFIER);
//				}
//				else
//				{
//					throw new XMPException("Top level property in wrong XMP_SchemaGroup",
//							XMPError.BADXMP);
//				}
//			}
//		}
//	}
//
//
//
//	/**
//	 * Add a node to the XMP tree from an XML element. Called recursively for
//	 * lower level children. The kind of node is determined from the value of
//	 * the "kind" XML attribute:
//	 * <ul>
//	 * <li>No kind attribute - A simple child, must have a "value" attribute.
//	 * <li>struct - A regular struct, the fields are contained elements
//	 * <li>c-struct - A compact struct, the fields are attributes.
//	 * <li>bag, seq, alt - A regular array, the items are contained elements.
//	 * <li>c-bag, c-seq, c-alt - A compact array, the items are catenated in a
//	 * "value" attribute.
//	 * <li>qual - A simple qualifier, must have a "value" attribute.
//	 * <li>*-qual - A qualifier with a struct or array value, the * part is
//	 * struct, c-struct, bag, ...
//	 * </ul>
//	 *
//	 * @param xmpParent the parent <code>XMPNode</code>
//	 * @param xmlNode the curernt XML node to parse
//	 * @param qualMode a modus that teels if a qualifier is expected, see constants
//	 * @throws XMPException Thrown if the node mal-formed.
//	 */
//	private static void addXMPNode(XMPNode xmpParent, Node xmlNode, int qualMode)
//			throws XMPException
//	{
//		// The three special attributes that might be present.
//		Node valueAttr = null;
//		Node kindAttr = null;
//		Node sepAttr = null;
//
//		// Scan for the special attributes. They might be anywhere among the XML
//		// node's attributes.
//		// Make sure all other attributes are in a namespace.
//		for (int i = 0; i < xmlNode.getAttributes().getLength(); i++)
//		{
//			Node attribute = xmlNode.getAttributes().item(i);
//			String attrName = attribute.getNodeName();
//			if ("value".equals(attrName))
//			{
//				valueAttr = attribute;
//			}
//			else if ("kind".equals(attrName))
//			{
//				kindAttr = attribute;
//			}
//			else if ("sep".equals(attrName))
//			{
//				sepAttr = attribute;
//			}
//			else if (attribute.getNamespaceURI() == null)
//			{
//				throw new XMPException("Non-special XML attribute must be in a namespace",
//						XMPError.BADXMP);
//			}
//		}
//
//		// Do semantic checks on the special attributes, determine the kind of
//		// child.
//		PropertyOptions childOpts = evaluateNodeType(qualMode, valueAttr, kindAttr, sepAttr);
//		boolean isQual = childOpts.isQualifier();
//		assert childOpts.isSimple()  ||  childOpts.isStruct()  ||  childOpts.isArray();
//		assert !(childOpts.isSimple()  &&  childOpts.isCompact());
//
//
//		// Perform some checks and add the child to the XMP parent.
//		String nodeLocalName = xmlNode.getLocalName();
//		String nodeNS = xmlNode.getNamespaceURI();
//		boolean isAlias = false;
//		if (NS_XML.equals(nodeNS)  ||
//			NS_PXMP.equals(nodeNS)  ||
//			NS_RDF.equals(nodeNS))
//		{
//			throw new XMPException("Improper use of xml:, pxmp:, or rdf: namespace",
//					XMPError.BADXMP);
//		}
//		else if (isQual)
//		{
//			if (nodeNS == null)
//			{
//				throw new XMPException("Qualifiers must be in a namespace", XMPError.BADXMP);
//			}
//			else if (!childOpts.isSimple()
//					&& ((XML_LANG.equals(xmlNode.getNodeName()))  ||
//						("type".equals(nodeLocalName) && NS_RDF.equals(nodeNS))  ||
//						("resource".equals(nodeLocalName) && NS_RDF.equals(nodeNS))))
//			{
//				throw new XMPException(
//						"xml:lang, rdf:type, and rdf:resource qualifiers must be simple",
//						XMPError.BADXMP);
//			}
//		}
//		else
//		{
//			if (xmpParent.getOptions().isArray())
//			{
//				if (!"item".equals(nodeLocalName))
//				{
//					throw new XMPException("Array item elements must be named 'item'",
//							XMPError.BADXMP);
//				}
//			}
//			else if (nodeNS == null)
//			{
//				throw new XMPException(
//					"Properties and fields, must be in a namespace", XMPError.BADXMP);
//			}
//
//
//			if (xmpParent.getOptions().isSchemaNode())
//			{
//				if (XMPMetaFactory.getSchemaRegistry().resolveAlias(nodeNS, nodeLocalName) != null)
//				{
//					isAlias = true;
//					xmpParent.setHasAliases(true);
//					xmpParent.getParent().setHasAliases(true);
//				}
//			}
//		}
//
//		XMPNode newNode;
//		if (childOpts.isSimple())
//		{
//			newNode = addSimpleNode(xmpParent, xmlNode, isQual, valueAttr, kindAttr);
//		}
//		else if (childOpts.isStruct())
//		{
//			newNode = addStructNode(xmpParent, xmlNode, childOpts, isQual, kindAttr);
//		}
//		else
//		{
//			// isArray
//			newNode = addArrayNode(xmpParent, xmlNode, childOpts, isQual, valueAttr, kindAttr,
//					sepAttr);
//		}
//		newNode.setAlias(isAlias);
//	}
//
//
//	/**
//	 * Parses a simle node.
//	 *
//	 * @param xmpParent the parent node
//	 * @param xmlNode the xml node describing the struct
//	 * @param isQual flag if node will be a qualifier
//	 * @param valueAttr the value attribute
//	 * @param kindAttr the kind attribute node
//	 * @return Returns the generated node.
//	 * @throws XMPException Thrown if node is mal-formed
//	 */
//	private static XMPNode addSimpleNode(XMPNode xmpParent, Node xmlNode,
//			boolean isQual, Node valueAttr, Node kindAttr) throws XMPException
//	{
//		assert valueAttr == null || "value".equals(valueAttr.getNodeName());
//		assert kindAttr == null || "kind".equals(kindAttr.getNodeName());
//
//		String nodeName = xmlNode.getNodeName();
//		String localName = xmlNode.getLocalName();
//		String namespace = xmlNode.getNamespaceURI();
//
//		XMPNode newNode = new XMPNode(getNodeName(xmlNode), null);
//
//		if (xmlNode.getNodeType() == Node.ATTRIBUTE_NODE)
//		{
//			// A simple child from an attribute has nothing more but the value.
//			assert valueAttr == null && kindAttr == null;
//			newNode.setValue(xmlNode.getNodeValue());
//		}
//		else
//		{
//			// A simple child from an element needs a value attribute and might
//			// have qualifiers.
//			assert xmlNode.getNodeType() == Node.ELEMENT_NODE;
//			assert valueAttr != null;
//			assert isQual == (kindAttr != null);
//
//			newNode.setValue(valueAttr.getNodeValue());
//
//			// Process the qualifiers. Simple ones are general attributes,
//			// compound ones are nested elements.
//			// Do the attributes first so that simple qualifiers from them are
//			// before compound qualifiers.
//			for (int i = 0; i < xmlNode.getAttributes().getLength(); i++)
//			{
//				Node xmlQualifier = xmlNode.getAttributes().item(i);
//				if (xmlQualifier != valueAttr && xmlQualifier != kindAttr)
//				{
//					addSimpleNode(newNode, xmlQualifier, true, null, null);
//				}
//
//			}
//
//			for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++)
//			{
//				Node xmlQualifier = xmlNode.getChildNodes().item(i);
//				if (xmlQualifier.getNodeType() == Node.ELEMENT_NODE)
//				{
//					addXMPNode(newNode, xmlQualifier, REQUIRE_QUALIFIER);
//				}
//			}
//		}
//
//		// Done, transfer the child to the parent and return.
//		if (!isQual)
//		{
//			xmpParent.addChild(newNode);
//
//		}
//		else
//		{
//			boolean isLang = XML_LANG.equals(nodeName);
//			boolean isType = "type".equals(localName) && NS_RDF.equals(namespace);
//			boolean isURI = "resource".equals(localName) && NS_RDF.equals(namespace);
//
//			if ((isLang | isType | isURI) && newNode.hasQualifier())
//			{
//				throw new XMPException(
//						"xml:lang, rdf:type, and rdf:resource qualifiers must be simple",
//						XMPError.BADXMP);
//			}
//			else if (isURI)
//			{
//				// The rdf:resource qualifier is stored as an option bit, not a
//				// regular qualifier.
//				if (xmpParent.getOptions().isCompositeProperty())
//				{
//					throw new XMPException("rdf:resource can only be applied to simple nodes",
//							XMPError.BADXMP);
//				}
//
//				xmpParent.getOptions().setURI(true);
//			}
//			else
//			{
//				xmpParent.addQualifier(newNode);
//			}
//		}
//
//		return newNode;
//	}
//
//
//	/**
//	 * Parses a struct node.
//	 *
//	 * @param xmpParent the parent node
//	 * @param xmlNode the xml node describing the struct
//	 * @param options the options for the new node
//	 * @param isQual flag if node will be a qualifier
//	 * @param kindAttr the kind attribute node
//	 * @return Returns the generated node.
//	 * @throws XMPException Thrown if node is mal-formed
//	 */
//	private static XMPNode addStructNode(XMPNode xmpParent, Node xmlNode, PropertyOptions options,
//			boolean isQual, Node kindAttr) throws XMPException
//	{
//		assert options.isStruct();
//		assert isQual == options.isQualifier();
//		assert kindAttr != null  &&  "kind".equals(kindAttr.getNodeName());
//
//		XMPNode newNode = new XMPNode(getNodeName(xmlNode), options);
//
//		// Process the fields of the struct.
//		if (options.isCompact())
//		{
//			// The fields of a compact struct are attributes of the parent. Any
//			// contained elements are qualifiers.
//			for (int i = 0; i < xmlNode.getAttributes().getLength(); i++)
//			{
//				Node xmlField = xmlNode.getAttributes().item(i);
//				if (xmlField != kindAttr)
//				{
//					addSimpleNode(newNode, xmlField, false, null, null);
//				}
//			}
//
//			for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++)
//			{
//				Node xmlQualifier = xmlNode.getAttributes().item(i);
//				if (xmlQualifier.getNodeType() == Node.ELEMENT_NODE)
//				{
//					addXMPNode(newNode, xmlQualifier, REQUIRE_QUALIFIER);
//				}
//			}
//		}
//		else
//		{
//			// The fields of a regular struct are contained elements, possibly mixed with
//			// qualifiers. General attributes of the parent node are simple qualifiers. Do the
//			// attributes first so that simple qualifiers from them are before compound qualifiers.
//			for (int i = 0; i < xmlNode.getAttributes().getLength(); i++)
//			{
//				Node xmlQualifier = xmlNode.getAttributes().item(i);
//				if (xmlQualifier != kindAttr)
//				{
//					addSimpleNode(newNode, xmlQualifier, true, null, null);
//				}
//			}
//
//			for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++)
//			{
//				Node xmlField = xmlNode.getChildNodes().item(i);
//				if (xmlField.getNodeType() == Node.ELEMENT_NODE)
//				{
//					addXMPNode(newNode, xmlField, ALLOW_QUALIFIER);
//				}
//			}
//		}
//
//		// Done, transfer the child to the parent.
//		if (!isQual)
//		{
//			xmpParent.addChild(newNode);
//		}
//		else
//		{
//			xmpParent.addQualifier(newNode);
//		}
//
//		return newNode;
//	}
//
//
//	/**
//	 * Parses an array node.
//	 *
//	 * @param xmpParent the parent node
//	 * @param xmlNode the xml node describing the struct
//	 * @param options the options for the new node
//	 * @param isQual flag if node will be a qualifier
//	 * @param valueAttr the value attribute node
//	 * @param kindAttr the kind attribute node
//	 * @param sepAttr the separator attribute node
//	 * @return Returns the generated node.
//	 * @throws XMPException Thrown if node is mal-formed
//	 */
//	private static XMPNode addArrayNode(XMPNode xmpParent, Node xmlNode, PropertyOptions options,
//			boolean isQual, Node valueAttr, Node kindAttr, Node sepAttr)
//			throws XMPException
//	{
//		assert xmlNode.getNodeType() == Node.ELEMENT_NODE;
//		assert options.isArray();
//		assert isQual == options.isQualifier();
//		assert (valueAttr == null) || ("value".equals(valueAttr.getNodeName()));
//		assert (kindAttr != null) && "kind".equals(kindAttr.getNodeName());
//		assert (sepAttr == null) || "sep".equals(sepAttr.getNodeName());
//		assert (sepAttr == null) || " ".equals(sepAttr.getNodeValue())
//				|| "; ".equals(sepAttr.getNodeValue());
//
//		XMPNode newNode = new XMPNode(getNodeName(xmlNode), options);
//
//		// The items of a regular array are contained "item" elements. Other contained elements of a
//		// regular array are compound qualifiers. The items of a compact array are catenated
//		// in the "value" attribute. All contained elements of a compact array are compound
//		// qualifiers. In either case, general attributes of the XML parent are simple qualifiers.
//		// Do the attributes first so that simple qualifiers from them are
//		// before compound qualifiers.
//
//		for (int i = 0; i < xmlNode.getAttributes().getLength(); i++)
//		{
//			Node xmlQualifier = xmlNode.getAttributes().item(i);
//			if (xmlQualifier != valueAttr  &&
//				xmlQualifier != kindAttr  &&
//				xmlQualifier != sepAttr)
//			{
//				addSimpleNode(newNode, xmlQualifier, true, null, null);
//			}
//		}
//
//		int qualMode = ALLOW_QUALIFIER;
//
//		if (options.isCompact())
//		{
//			qualMode = REQUIRE_QUALIFIER;
//			addCompactArrayItems(newNode, valueAttr, sepAttr);
//		}
//
//		for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++)
//		{
//			Node xmlElement = xmlNode.getChildNodes().item(i);
//			if (xmlElement.getNodeType() == Node.ELEMENT_NODE)
//			{
//				addXMPNode(newNode, xmlElement, qualMode);
//			}
//		}
//
//
//		if (!options.isCompact() && options.isArrayAlternate())
//		{
//			XMPNodeUtils.detectAltText(newNode);
//		}
//
//		// Done, transfer the child to the parent.
//		if (!isQual)
//		{
//			xmpParent.addChild(newNode);
//		}
//		else
//		{
//			xmpParent.addQualifier(newNode);
//		}
//
//		return newNode;
//	}
//
//
//
//	/**
//	 * Parses a compact array node.
//	 * @param xmpArray the XMP array node to fill
//	 * @param valueAttr the value attribute node
//	 * @param sepAttr the value attribute node
//	 * @throws XMPException Thrown if node is mal-formed
//	 */
//	private static void addCompactArrayItems(XMPNode xmpArray, Node valueAttr, Node sepAttr)
//			throws XMPException
//	{
//		assert xmpArray.getOptions().isArray();
//
//		String fullValue = valueAttr.getNodeValue();
//		if (sepAttr == null || " ".equals(sepAttr.getNodeValue()))
//		{
//			int start = 0, end = 0;
//			while (end < fullValue.length())
//			{
//			 	boolean isWhitespace = Character.isWhitespace(fullValue.charAt(end));
//				if (isWhitespace || end == fullValue.length() - 1)
//				{
//					if (end != start)
//					{
//						if (!isWhitespace)
//						{
//							end++;
//						}
//						XMPNode newItem = new XMPNode(ARRAY_ITEM_NAME, fullValue
//								.substring(start, end), null);
//						xmpArray.addChild(newItem);
//					}
//					end++;
//					start = end;
//				}
//				else
//				{
//					end++;
//				}
//			}
//		}
//		else
//		{
//			int itemEnd;
//			for (int itemStart = 0; itemStart < fullValue.length(); itemStart = itemEnd + 2)
//			{
//				for (itemEnd = itemStart; itemEnd < fullValue.length(); itemEnd++)
//				{
//					if (fullValue.charAt(itemEnd) == ';' && (itemEnd + 1) < fullValue.length())
//					{
//						char next = fullValue.charAt(itemEnd + 1);
//						if (next == ' ')
//						{
//							break;
//						}
//						else if (next == ';')
//						{
//							itemEnd++;
//						}
//					}
//				}
//				String value = fullValue.substring(itemStart, itemEnd);
//				if (value.indexOf(";;") >= 0)
//				{
//					value = value.replaceAll(";;", ";");
//				}
//				XMPNode newItem = new XMPNode(ARRAY_ITEM_NAME, value, null);
//				xmpArray.addChild(newItem);
//			}
//		}
//	}
//
//
//	/**
//	 * Evaluates the the node type performs some checks.
//	 *
//	 * @param qualMode the "need-qualifier"-mode
//	 * @param valueAttr the value attribute
//	 * @param kindAttr the kind attribute
//	 * @param sepAttr the separator attribute
//	 * @return Returns the new child options which are set appropriate
//	 * @throws XMPException Thrown if the node is mal-formed.
//	 */
//	private static PropertyOptions evaluateNodeType(int qualMode, Node valueAttr, Node kindAttr,
//			Node sepAttr) throws XMPException
//	{
//		PropertyOptions childOpts;
//		assert !"".equals(kindAttr); // make sure that it can't be empty
//		if (kindAttr == null)
//		{
//			// This must be a simple property or struct field.
//			if (qualMode == REQUIRE_QUALIFIER)
//			{
//				throw new XMPException("Simple qualifier element must have a \"kind\" attribute",
//						XMPError.BADXMP);
//			}
//			if (valueAttr == null)
//			{
//				throw new XMPException("Simple child element must have a \"value\" attribute",
//						XMPError.BADXMP);
//			}
//			if (sepAttr != null)
//			{
//				throw new XMPException("Simple child element has a \"sep\" attribute",
//						XMPError.BADXMP);
//			}
//
//			childOpts = new PropertyOptions();
//		}
//		else if ("qual".equals(kindAttr.getNodeValue()))
//		{
//			// This must be a simple qualifier.
//			if (qualMode == REQUIRE_NON_QUALIFIER)
//			{
//				throw new XMPException("Unexpected simple qualifier element", XMPError.BADXMP);
//			}
//			if (valueAttr == null)
//			{
//				throw new XMPException("Simple qualifier element must have a \"value\" attribute",
//						XMPError.BADXMP);
//			}
//			if (sepAttr != null)
//			{
//				throw new XMPException("Simple qualifier element has a \"sep\" attribute",
//						XMPError.BADXMP);
//			}
//			childOpts = new PropertyOptions(PropertyOptions.QUALIFIER);
//		}
//		else
//		{
//			// can be anything
//			childOpts = evaluateKindAttribute(qualMode, valueAttr, kindAttr, sepAttr);
//		}
//
//		return childOpts;
//	}
//
//
//	/**
//	 * Evaluates the kind-attribute and perform some checks.
//	 *
//	 * @param qualMode the "need-qualifier"-mode
//	 * @param valueAttr the value attribute
//	 * @param kindAttr the kind attribute
//	 * @param sepAttr the separator attribute
//	 * @return Returns the new child options which are set appropriate.
//	 * @throws XMPException Thrown if the kind attribute is not recognized
//	 */
//	private static PropertyOptions evaluateKindAttribute(int qualMode, Node valueAttr,
//			Node kindAttr, Node sepAttr) throws XMPException
//	{
//		PropertyOptions childOpts;
//		// This must be a struct or an array. It might also be compact, it
//		// might also be a qualifier.
//		// "c-...", "c-...-qual", "..." or "...-qual"
//		childOpts = new PropertyOptions();
//		String kindStr = kindAttr.getNodeValue();
//		if (kindStr.startsWith("c-"))
//		{
//			// This is a compact struct or array.
//			childOpts.setCompact(true);
//			kindStr = kindStr.substring(2);
//		}
//
//		boolean compoundQual = false;
//		if (kindStr.endsWith("-qual"))
//		{
//			compoundQual = true;
//			kindStr = kindStr.substring(0, kindStr.length() - 5);
//		}
//		else if (kindStr.indexOf('-') >= 0)
//		{
//			throw new XMPException("Bad suffix for \"kind\" attribute value", XMPError.BADXMP);
//		}
//
//
//		switch (qualMode)
//		{
//			case REQUIRE_QUALIFIER:
//				if (compoundQual)
//				{
//					childOpts.setQualifier(true);
//				}
//				else
//				{
//					throw new XMPException(
//							"Compound qualifier element missing \"-qual\" kind suffix",
//							XMPError.BADXMP);
//				}
//				break;
//
//			case REQUIRE_NON_QUALIFIER:
//				if (compoundQual)
//				{
//					throw new XMPException(
//						"Unexpected compound qualifier element", XMPError.BADXMP);
//				}
//				else if ("qual".equals(kindStr))
//				{
//					throw new XMPException("Unexpected simple qualifier element", XMPError.BADXMP);
//				}
//
//				break;
//
//			case ALLOW_QUALIFIER:
//			default:
//				// do nothing
//		}
//
//
//		// Determine if this is a struct, or determine the array form.
//		if ("struct".equals(kindStr))
//		{
//			childOpts.setStruct(true);
//		}
//		else if ("bag".equals(kindStr))
//		{
//			childOpts.setArray(true);
//		}
//		else if ("seq".equals(kindStr))
//		{
//			childOpts.setArray(true).setArrayOrdered(true);
//		}
//		else if ("alt".equals(kindStr))
//		{
//			childOpts.setArray(true).setArrayOrdered(true).setArrayAlternate(true);
//		}
//		else
//		{
//			throw new XMPException("Bad value for 'kind' attribute", XMPError.BADXMP);
//		}
//
//		if (childOpts.isArray()  &&  childOpts.isCompact())
//		{
//			if (valueAttr == null)
//			{
//				throw new XMPException("Compact array must have a 'value' attribute",
//						XMPError.BADXMP);
//			}
//		}
//		else
//		{
//			if (valueAttr != null)
//			{
//				throw new XMPException(
//						"Struct or regular array can't have a 'value' attribute",
//						XMPError.BADXMP);
//			}
//			else if (sepAttr != null)
//			{
//				throw new XMPException("Only compact arrays may have a 'sep' attribute",
//						XMPError.BADXMP);
//			}
//		}
//		return childOpts;
//	}
//
//
//	/**
//	 * Registers the prefix if not exising and fixes the node name.
//	 *
//	 * @param xmlNode an XML Property Node
//	 * @return Returns the normalized name.
//	 * @throws XMPException Forwards XMP exceptions.
//	 */
//	private static String getNodeName(Node xmlNode) throws XMPException
//	{
//		if (!"item".equals(xmlNode.getNodeName()))
//		{
//			XMPSchemaRegistry registry = XMPMetaFactory.getSchemaRegistry();
//			String namespace = xmlNode.getNamespaceURI();
//			if (NS_DC_DEPRECATED.equals(namespace))
//			{
//				// Fix a legacy DC namespace
//				// FfF: remove it after CS3
//				namespace = NS_DC;
//			}
//			String prefix = registry.getNamespacePrefix(namespace);
//			if (prefix == null)
//			{
//				prefix = registry.registerNamespace(namespace, xmlNode.getPrefix());
//			}
//			return prefix + xmlNode.getLocalName();
//		}
//		else
//		{
//			return ARRAY_ITEM_NAME;
//		}
//	}
//}