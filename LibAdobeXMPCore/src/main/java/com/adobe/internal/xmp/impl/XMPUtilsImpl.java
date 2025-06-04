// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================



package com.adobe.internal.xmp.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.adobe.internal.xmp.XMPConst;
import com.adobe.internal.xmp.XMPError;
import com.adobe.internal.xmp.XMPException;
import com.adobe.internal.xmp.XMPMeta;
import com.adobe.internal.xmp.XMPMetaFactory;
import com.adobe.internal.xmp.XMPSchemaRegistry;
import com.adobe.internal.xmp.XMPUtils;
import com.adobe.internal.xmp.impl.xpath.XMPPath;
import com.adobe.internal.xmp.impl.xpath.XMPPathParser;
import com.adobe.internal.xmp.options.PropertyOptions;
import com.adobe.internal.xmp.options.SerializeOptions;
import com.adobe.internal.xmp.options.TemplateOptions;
import com.adobe.internal.xmp.properties.XMPAliasInfo;



/**
 * @author Stefan Makswit
 * @version $Revision$
 * @since 11.08.2006
 */
public class XMPUtilsImpl implements XMPConst
{
	/** */
	private static final int UCK_NORMAL = 0;
	/** */
	private static final int UCK_SPACE = 1;
	/** */
	private static final int UCK_COMMA = 2;
	/** */
	private static final int UCK_SEMICOLON = 3;
	/** */
	private static final int UCK_QUOTE = 4;
	/** */
	private static final int UCK_CONTROL = 5;


	/**
	 * Private constructor, as
	 */
	private XMPUtilsImpl()
	{
		// EMPTY
	}


	/**
	 * @see XMPUtils#catenateArrayItems(XMPMeta, String, String, String, String,
	 *      boolean)
	 *
	 * @param xmp
	 *            The XMP object containing the array to be catenated.
	 * @param schemaNS
	 *            The schema namespace URI for the array. Must not be null or
	 *            the empty string.
	 * @param arrayName
	 *            The name of the array. May be a general path expression, must
	 *            not be null or the empty string. Each item in the array must
	 *            be a simple string value.
	 * @param separator
	 *            The string to be used to separate the items in the catenated
	 *            string. Defaults to &quot;; &quot;, ASCII semicolon and space
	 *            (U+003B, U+0020).
	 * @param quotes
	 *            The characters to be used as quotes around array items that
	 *            contain a separator. Defaults to &apos;&quot;&apos;
	 * @param allowCommas
	 *            Option flag to control the catenation.
	 * @return Returns the string containing the catenated array items.
	 * @throws XMPException
	 *             Forwards the Exceptions from the metadata processing
	 */
	public static String catenateArrayItems(XMPMeta xmp, String schemaNS, String arrayName,
			String separator, String quotes, boolean allowCommas) throws XMPException
	{
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertArrayName(arrayName);
		ParameterAsserts.assertImplementation(xmp);
		if (separator == null  ||  separator.length() == 0)
		{
			separator = "; ";
		}
		if (quotes == null  ||  quotes.length() == 0)
		{
			quotes = "\"";
		}

		XMPMetaImpl xmpImpl = (XMPMetaImpl) xmp;
		XMPNode arrayNode = null;
		XMPNode currItem = null;

		// Return an empty result if the array does not exist,
		// hurl if it isn't the right form.
		XMPPath arrayPath = XMPPathParser.expandXPath(schemaNS, arrayName);
		arrayNode = XMPNodeUtils.findNode(xmpImpl.getRoot(), arrayPath, false, null);
		if (arrayNode == null)
		{
			return "";
		}
		else if (!arrayNode.getOptions().isArray() || arrayNode.getOptions().isArrayAlternate())
		{
			throw new XMPException("Named property must be non-alternate array", XMPError.BADPARAM);
		}

		// Make sure the separator is OK.
		checkSeparator(separator);
		// Make sure the open and close quotes are a legitimate pair.
		char openQuote = quotes.charAt(0);
		char closeQuote = checkQuotes(quotes, openQuote);

		// Build the result, quoting the array items, adding separators.
		// Hurl if any item isn't simple.

		StringBuffer catinatedString = new StringBuffer();

		for (Iterator it = arrayNode.iterateChildren(); it.hasNext();)
		{
			currItem = (XMPNode) it.next();
			if (currItem.getOptions().isCompositeProperty())
			{
				throw new XMPException("Array items must be simple", XMPError.BADPARAM);
			}
			String str = applyQuotes(currItem.getValue(), openQuote, closeQuote, allowCommas);

			catinatedString.append(str);
			if (it.hasNext())
			{
				catinatedString.append(separator);
			}
		}

		return catinatedString.toString();
	}


	/**
	 * see {@link XMPUtils#separateArrayItems(XMPMeta, String, String, String,
	 * PropertyOptions, boolean)}
	 *
	 * @param xmp
	 *            The XMP object containing the array to be updated.
	 * @param schemaNS
	 *            The schema namespace URI for the array. Must not be null or
	 *            the empty string.
	 * @param arrayName
	 *            The name of the array. May be a general path expression, must
	 *            not be null or the empty string. Each item in the array must
	 *            be a simple string value.
	 * @param catedStr
	 *            The string to be separated into the array items.
	 * @param arrayOptions
	 *            Option flags to control the separation.
	 * @param preserveCommas
	 *            Flag if commas shall be preserved
	 *
	 * @throws XMPException
	 *             Forwards the Exceptions from the metadata processing
	 */
	public static void separateArrayItems(XMPMeta xmp, String schemaNS, String arrayName,
			String catedStr, PropertyOptions arrayOptions, boolean preserveCommas)
			throws XMPException
	{
		
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertArrayName(arrayName);
		if (catedStr == null)
		{
			throw new XMPException("Parameter must not be null", XMPError.BADPARAM);
		}
		ParameterAsserts.assertImplementation(xmp);
		XMPMetaImpl xmpImpl = (XMPMetaImpl) xmp;

		// Keep a zero value, has special meaning below.
		XMPNode arrayNode = separateFindCreateArray(schemaNS, arrayName, arrayOptions, xmpImpl);
		
		int arrayElementLimit = Integer.MAX_VALUE;
		if(arrayNode != null)
		{
			if(arrayOptions != null) 
			{
				arrayElementLimit = arrayOptions.getArrayElementsLimit();
				if(arrayElementLimit == -1 )
				{
					arrayElementLimit = Integer.MAX_VALUE;
				}
			}
		}
		// Extract the item values one at a time, until the whole input string is done.
		StringBuilder itemValue = new StringBuilder("");
		int itemStart, itemEnd;
		int nextKind = UCK_NORMAL, charKind = UCK_NORMAL;
		char ch = 0, nextChar = 0;

		itemEnd = 0;
		int endPos = catedStr.length();
		while (itemEnd < endPos)
		{
			// Skip any leading spaces and separation characters. Always skip commas here.
			// They can be kept when within a value, but not when alone between values.
			
			if( arrayNode.getChildrenLength() >= arrayElementLimit)
			{
				break;
			}
			
			for (itemStart = itemEnd; itemStart < endPos; itemStart++)
			{
				ch = catedStr.charAt(itemStart);
				charKind = classifyCharacter(ch);
				if (charKind == UCK_NORMAL || charKind == UCK_QUOTE)
				{
					break;
				}
			}
			if (itemStart >= endPos)
			{
				break;
			}

			if (charKind != UCK_QUOTE)
			{
				// This is not a quoted value. Scan for the end, create an array
				// item from the substring.
				for (itemEnd = itemStart; itemEnd < endPos; itemEnd++)
				{
					ch = catedStr.charAt(itemEnd);
					charKind = classifyCharacter(ch);

					if (charKind == UCK_NORMAL || charKind == UCK_QUOTE  ||
						(charKind == UCK_COMMA && preserveCommas))
					{
						continue;
					}
					else if (charKind != UCK_SPACE)
					{
						break;
					}
					else if ((itemEnd + 1) < endPos)
					{
						ch = catedStr.charAt(itemEnd + 1);
						nextKind = classifyCharacter(ch);
						if (nextKind == UCK_NORMAL  ||  nextKind == UCK_QUOTE  ||
							(nextKind == UCK_COMMA && preserveCommas))
						{
							continue;
						}
					}

					// Anything left?
					break; // Have multiple spaces, or a space followed by a
							// separator.
				}
				itemValue = new StringBuilder(catedStr.substring(itemStart, itemEnd));
			}
			else
			{
				// Accumulate quoted values into a local string, undoubling
				// internal quotes that
				// match the surrounding quotes. Do not undouble "unmatching"
				// quotes.

				char openQuote = ch;
				char closeQuote = getClosingQuote(openQuote);

				itemStart++; // Skip the opening quote;
				itemValue = new StringBuilder("");

				for (itemEnd = itemStart; itemEnd < endPos; itemEnd++)
				{
					ch = catedStr.charAt(itemEnd);
					charKind = classifyCharacter(ch);

					if (charKind != UCK_QUOTE || !isSurroundingQuote(ch, openQuote, closeQuote))
					{
						// This is not a matching quote, just append it to the
						// item value.
						itemValue.append(ch);
					}
					else
					{
						// This is a "matching" quote. Is it doubled, or the
						// final closing quote?
						// Tolerate various edge cases like undoubled opening
						// (non-closing) quotes,
						// or end of input.

						if ((itemEnd + 1) < endPos)
						{
							nextChar = catedStr.charAt(itemEnd + 1);
							nextKind = classifyCharacter(nextChar);
						}
						else
						{
							nextKind = UCK_SEMICOLON;
							nextChar = 0x3B;
						}

						if (ch == nextChar)
						{
							// This is doubled, copy it and skip the double.
							itemValue.append(ch);
							// Loop will add in charSize.
							itemEnd++;
						}
						else if (!isClosingingQuote(ch, openQuote, closeQuote))
						{
							// This is an undoubled, non-closing quote, copy it.
							itemValue.append(ch);
						}
						else
						{
							// This is an undoubled closing quote, skip it and
							// exit the loop.
							itemEnd++;
							break;
						}
					}
				}
			}

			// Add the separated item to the array.
			// Keep a matching old value in case it had separators.
			int foundIndex = -1;
			for (int oldChild = 1; oldChild <= arrayNode.getChildrenLength(); oldChild++)
			{
				if (itemValue.toString().equals(arrayNode.getChild(oldChild).getValue()))
				{
					foundIndex = oldChild;
					break;
				}
			}

			XMPNode newItem = null;
			if (foundIndex < 0)
			{
				newItem = new XMPNode(ARRAY_ITEM_NAME, itemValue.toString(), null);
				arrayNode.addChild(newItem);
			}
// <#AdobePrivate>
//			else
//			{
//				newItem = arrayNode.getChild(foundIndex);
//				// Don't match again, let duplicates be seen.
//				arrayNode.getChild(foundIndex).setValue(null);
//			}
//			 </#AdobePrivate>
		}
	}


	/**
	 * Utility to find or create the array used by <code>separateArrayItems()</code>.
	 * @param schemaNS a the namespace fo the array
	 * @param arrayName the name of the array
	 * @param arrayOptions the options for the array if newly created
	 * @param xmp the xmp object
	 * @return Returns the array node.
	 * @throws XMPException Forwards exceptions
	 */
	private static XMPNode separateFindCreateArray(String schemaNS, String arrayName,
			PropertyOptions arrayOptions, XMPMetaImpl xmp) throws XMPException
	{
		arrayOptions = XMPNodeUtils.verifySetOptions(arrayOptions, null);
		if (!arrayOptions.isOnlyArrayOptions())
		{
			throw new XMPException("Options can only provide array form", XMPError.BADOPTIONS);
		}

		// Find the array node, make sure it is OK. Move the current children
		// aside, to be readded later if kept.
		XMPPath arrayPath = XMPPathParser.expandXPath(schemaNS, arrayName);
		XMPNode arrayNode = XMPNodeUtils.findNode(xmp.getRoot(), arrayPath, false, null);
		if (arrayNode != null)
		{
			// The array exists, make sure the form is compatible. Zero
			// arrayForm means take what exists.
			PropertyOptions arrayForm = arrayNode.getOptions();
			if (!arrayForm.isArray() || arrayForm.isArrayAlternate())
			{
				throw new XMPException("Named property must be non-alternate array",
					XMPError.BADXPATH);
			}
			if (arrayOptions.equalArrayTypes(arrayForm))
			{
				throw new XMPException("Mismatch of specified and existing array form",
						XMPError.BADXPATH); // *** Right error?
			}
		}
		else
		{
			// The array does not exist, try to create it.
			// don't modify the options handed into the method
			arrayNode = XMPNodeUtils.findNode(xmp.getRoot(), arrayPath, true, arrayOptions
					.setArray(true));
			if (arrayNode == null)
			{
				throw new XMPException("Failed to create named array", XMPError.BADXPATH);
			}
		}
		return arrayNode;
	}


	/**
	 * @see XMPUtils#removeProperties(XMPMeta, String, String, boolean, boolean)
	 *
	 * @param xmp
	 *            The XMP object containing the properties to be removed.
	 *
	 * @param schemaNS
	 *            Optional schema namespace URI for the properties to be
	 *            removed.
	 *
	 * @param propName
	 *            Optional path expression for the property to be removed.
	 *
	 * @param doAllProperties
	 *            Option flag to control the deletion: do internal properties in
	 *            addition to external properties.
	 * @param includeAliases
	 *            Option flag to control the deletion: Include aliases in the
	 *            "named schema" case above.
	 * @throws XMPException If metadata processing fails
	 */
	public static void removeProperties(XMPMeta xmp, String schemaNS, String propName,
			boolean doAllProperties, boolean includeAliases) throws XMPException
	{
		ParameterAsserts.assertImplementation(xmp);
		XMPMetaImpl xmpImpl = (XMPMetaImpl) xmp;

		if (propName != null && propName.length() > 0)
		{
			// Remove just the one indicated property. This might be an alias,
			// the named schema might not actually exist. So don't lookup the
			// schema node.

			if (schemaNS == null || schemaNS.length() == 0)
			{
				throw new XMPException("Property name requires schema namespace",
					XMPError.BADPARAM);
			}

			XMPPath expPath = XMPPathParser.expandXPath(schemaNS, propName);

			XMPNode propNode = XMPNodeUtils.findNode(xmpImpl.getRoot(), expPath, false, null);
			if (propNode != null)
			{
				if (doAllProperties
						|| !Utils.isInternalProperty(expPath.getSegment(XMPPath.STEP_SCHEMA)
								.getName(), expPath.getSegment(XMPPath.STEP_ROOT_PROP).getName()))
				{
					XMPNode parent = propNode.getParent();
					parent.removeChild(propNode);
					if (parent.getOptions().isSchemaNode()  &&  !parent.hasChildren())
					{
						// remove empty schema node
						parent.getParent().removeChild(parent);
					}

				}
			}
		}
		else if (schemaNS != null && schemaNS.length() > 0)
		{

			// Remove all properties from the named schema. Optionally include
			// aliases, in which case
			// there might not be an actual schema node.

			// XMP_NodePtrPos schemaPos;
			XMPNode schemaNode = XMPNodeUtils.findSchemaNode(xmpImpl.getRoot(), schemaNS, false);
			if (schemaNode != null)
			{
				if (removeSchemaChildren(schemaNode, doAllProperties))
				{
					xmpImpl.getRoot().removeChild(schemaNode);
				}
			}

			if (includeAliases)
			{
				// We're removing the aliases also. Look them up by their
				// namespace prefix.
				// But that takes more code and the extra speed isn't worth it.
				// Lookup the XMP node
				// from the alias, to make sure the actual exists.

				XMPAliasInfo[] aliases = XMPMetaFactory.getSchemaRegistry().findAliases(schemaNS);
				for (int i = 0; i < aliases.length; i++)
				{
					XMPAliasInfo info = aliases[i];
					XMPPath path = XMPPathParser.expandXPath(info.getNamespace(), info
							.getPropName());
					XMPNode actualProp = XMPNodeUtils
							.findNode(xmpImpl.getRoot(), path, false, null);
					if (actualProp != null)
					{
						XMPNode parent = actualProp.getParent();
						parent.removeChild(actualProp);
					}
				}
			}
		}
		else
		{
			// Remove all appropriate properties from all schema. In this case
			// we don't have to be
			// concerned with aliases, they are handled implicitly from the
			// actual properties.
			for (Iterator it = xmpImpl.getRoot().iterateChildren(); it.hasNext();)
			{
				XMPNode schema = (XMPNode) it.next();
				if (removeSchemaChildren(schema, doAllProperties))
				{
					it.remove();
				}
			}
		}
	}


	/**
	 * @see XMPUtils#appendProperties(XMPMeta, XMPMeta, boolean, boolean)
	 * @param source The source XMP object.
	 * @param destination The destination XMP object.
	 * @param doAllProperties Do internal properties in addition to external properties.
	 * @param replaceOldValues Replace the values of existing properties.
	 * @param deleteEmptyValues Delete destination values if source property is empty.
	 * @throws XMPException Forwards the Exceptions from the metadata processing
	 */
	public static void appendProperties(XMPMeta source, XMPMeta destination,
			boolean doAllProperties, boolean replaceOldValues, boolean deleteEmptyValues)
		throws XMPException
	{
		ParameterAsserts.assertImplementation(source);
		ParameterAsserts.assertImplementation(destination);

		XMPMetaImpl src = (XMPMetaImpl) source;
		XMPMetaImpl dest = (XMPMetaImpl) destination;

		for (Iterator it = src.getRoot().iterateChildren(); it.hasNext();)
		{
			XMPNode sourceSchema = (XMPNode) it.next();

			// Make sure we have a destination schema node
			XMPNode destSchema = XMPNodeUtils.findSchemaNode(dest.getRoot(),
					sourceSchema.getName(), false);
			boolean createdSchema = false;
			if (destSchema == null)
			{
				destSchema = new XMPNode(sourceSchema.getName(), sourceSchema.getValue(),
						new PropertyOptions().setSchemaNode(true));
				dest.getRoot().addChild(destSchema);
				createdSchema = true;
			}

			// Process the source schema's children.
			for (Iterator ic = sourceSchema.iterateChildren(); ic.hasNext();)
			{
				XMPNode sourceProp = (XMPNode) ic.next();
				if (doAllProperties
						|| !Utils.isInternalProperty(sourceSchema.getName(), sourceProp.getName()))
				{
					appendSubtree(
						dest, sourceProp, destSchema, false ,replaceOldValues, deleteEmptyValues);
				}
			}

			if (!destSchema.hasChildren()  &&  (createdSchema  ||  deleteEmptyValues))
			{
				// Don't create an empty schema / remove empty schema.
				dest.getRoot().removeChild(destSchema);
			}
		}
	}


	/**
	 * Remove all schema children according to the flag
	 * <code>doAllProperties</code>. Empty schemas are automatically remove
	 * by <code>XMPNode</code>
	 *
	 * @param schemaNode
	 *            a schema node
	 * @param doAllProperties
	 *            flag if all properties or only externals shall be removed.
	 * @return Returns true if the schema is empty after the operation.
	 */
	private static boolean removeSchemaChildren(XMPNode schemaNode, boolean doAllProperties)
	{
		for (Iterator it = schemaNode.iterateChildren(); it.hasNext();)
		{
			XMPNode currProp = (XMPNode) it.next();
			if (doAllProperties
					|| !Utils.isInternalProperty(schemaNode.getName(), currProp.getName()))
			{
				it.remove();
			}
		}

		return !schemaNode.hasChildren();
	}


	/**
	 * @see XMPUtilsImpl#appendProperties(XMPMeta, XMPMeta, boolean, boolean, boolean)
	 * @param destXMP The destination XMP object.
	 * @param sourceNode the source node
	 * @param destParent the parent of the destination node
	 * @param replaceOldValues Replace the values of existing properties.
	 * @param deleteEmptyValues flag if properties with empty values should be deleted
	 * 		   in the destination object.
	 * @throws XMPException
	 */
	private static void appendSubtree(XMPMetaImpl destXMP, XMPNode sourceNode, XMPNode destParent, boolean mergeCompound, 
			boolean replaceOldValues, boolean deleteEmptyValues) throws XMPException
	{
		XMPNode destNode = XMPNodeUtils.findChildNode(destParent, sourceNode.getName(), false);

		boolean valueIsEmpty = false;
		
		valueIsEmpty = sourceNode.getOptions().isSimple() ?
				sourceNode.getValue() == null  ||  sourceNode.getValue().length() == 0 :
				!sourceNode.hasChildren();
		
		
		if(valueIsEmpty){
			if ( deleteEmptyValues && (destNode != null) ) {
				destParent.removeChild(destNode);
			}
			return; // ! Done, empty values are either ignored or cause deletions.
		}
		
		if (destNode == null)
		{
			// The one easy case, the destination does not exist.
			XMPNode tempNode = (XMPNode) sourceNode.clone(true);
			if(tempNode != null)
				destParent.addChild(tempNode);
			return;
		}
		
		PropertyOptions sourceForm = sourceNode.getOptions();
		
		boolean replaceThis = replaceOldValues;	// ! Don't modify replaceOld, it gets passed to inner calls.
		if ( mergeCompound && (! sourceForm.isSimple()) ) {
			replaceThis = false;
		}
		
		if (replaceThis)
		{
			// The destination exists and should be replaced.
		//	destXMP.setNode(destNode, sourceNode.getValue(), sourceNode.getOptions(), true);
			destParent.removeChild(destNode);
			//destNode = (XMPNode) sourceNode.clone();
			XMPNode tempNode = (XMPNode) sourceNode.clone(true);
			if(tempNode != null)
				destParent.addChild(tempNode);
			return;
		}
		
		// The destination exists and is not totally replaced. Structs and
		// arrays are merged.

		
		PropertyOptions destForm = destNode.getOptions();
		if (sourceForm.getOptions() != destForm.getOptions() || sourceForm.isSimple())
		{
			return;
		}
		
		if (sourceForm.isStruct())
		{
			// To merge a struct process the fields recursively. E.g. add simple missing fields.
			// The recursive call to AppendSubtree will handle deletion for fields with empty
			// values.
			for (Iterator it = sourceNode.iterateChildren(); it.hasNext();)
			{
				XMPNode sourceField = (XMPNode) it.next();
				appendSubtree(destXMP, sourceField, destNode, mergeCompound,
					replaceOldValues, deleteEmptyValues);
				if (deleteEmptyValues  &&  !destNode.hasChildren())
				{
					destParent.removeChild(destNode);
				}
			}
		}
		else if (sourceForm.isArrayAltText())
		{
			// Merge AltText arrays by the "xml:lang" qualifiers. Make sure x-default is first.
			// Make a special check for deletion of empty values. Meaningful in AltText arrays
			// because the "xml:lang" qualifier provides unambiguous source/dest correspondence.
			for (Iterator it = sourceNode.iterateChildren(); it.hasNext();)
			{
				XMPNode sourceItem = (XMPNode) it.next();
				if (!sourceItem.hasQualifier()
						|| !XMPConst.XML_LANG.equals(sourceItem.getQualifier(1).getName()))
				{
					continue;
				}
				int destIndex = XMPNodeUtils.lookupLanguageItem(destNode,
					sourceItem.getQualifier(1).getValue());
				
				if(sourceItem.getValue() == null || sourceItem.getValue().length() == 0){
					if ( deleteEmptyValues && (destIndex != -1) ) {
						destNode.removeChild(destIndex);
						if (!destNode.hasChildren())
						{
							destParent.removeChild(destNode);
						}
					}
				}
				else {
					if (destIndex == -1) {
						// Not replacing, keep the existing item.
						if (!XMPConst.X_DEFAULT.equals(sourceItem.getQualifier(1).getValue())
								|| !destNode.hasChildren()) {
							XMPNode tempNode = (XMPNode) sourceItem.clone(true);
							if (tempNode != null)
								destNode.addChild(tempNode);
							// sourceItem.cloneSubtree(destNode);
						} else {
							XMPNode destItem = new XMPNode(sourceItem.getName(), sourceItem.getValue(),
									sourceItem.getOptions());
							sourceItem.cloneSubtree(destItem, true);
							destNode.addChild(1, destItem);
						}
					}
					else{
						if(replaceOldValues)
							destNode.getChild(destIndex).setValue(sourceItem.getValue());
					}
				}
			}
		}
		else if (sourceForm.isArray())
		{
			// Merge other arrays by item values. Don't worry about order or duplicates. Source
			// items with empty values do not cause deletion, that conflicts horribly with
			// merging.
				for (Iterator is = sourceNode.iterateChildren(); is.hasNext();)
			{
				XMPNode sourceItem = (XMPNode) is.next();
					boolean match = false;
				for (Iterator id = destNode.iterateChildren(); id.hasNext();)
				{
					XMPNode destItem = (XMPNode) id.next();
					if (itemValuesMatch(sourceItem, destItem))
					{
						match = true;
						break;
						}
					}
					if (!match)
					{
						XMPNode tempNode = (XMPNode) sourceItem.clone(true);
						if(tempNode != null)
							destNode.addChild(tempNode);
					}
				}
			}
		}
	


	/**
	 * Compares two nodes including its children and qualifier.
	 * @param leftNode an <code>XMPNode</code>
	 * @param rightNode an <code>XMPNode</code>
	 * @return Returns true if the nodes are equal, false otherwise.
	 * @throws XMPException Forwards exceptions to the calling method.
	 */
	private static boolean itemValuesMatch(XMPNode leftNode, XMPNode rightNode) throws XMPException
	{
		PropertyOptions leftForm = leftNode.getOptions();
		PropertyOptions rightForm = rightNode.getOptions();

		if (!leftForm.equals(rightForm))
		{
			return false;
		}

		if (leftForm.isSimple())
		{
			// Simple nodes, check the values and xml:lang qualifiers.
			if (!leftNode.getValue().equals(rightNode.getValue()))
			{
				return false;
			}
			if (leftNode.getOptions().getHasLanguage() != rightNode.getOptions().getHasLanguage())
			{
				return false;
			}
			if (leftNode.getOptions().getHasLanguage()
					&& !leftNode.getQualifier(1).getValue().equals(
							rightNode.getQualifier(1).getValue()))
			{
				return false;
			}
		}
		else if (leftForm.isStruct())
		{
			// Struct nodes, see if all fields match, ignoring order.

			if (leftNode.getChildrenLength() != rightNode.getChildrenLength())
			{
				return false;
			}

			for (Iterator it = leftNode.iterateChildren(); it.hasNext();)
			{
				XMPNode leftField = (XMPNode) it.next();
				XMPNode rightField = XMPNodeUtils.findChildNode(rightNode, leftField.getName(),
						false);
				if (rightField == null || !itemValuesMatch(leftField, rightField))
				{
					return false;
				}
			}
		}
		else
		{
			// Array nodes, see if the "leftNode" values are present in the
			// "rightNode", ignoring order, duplicates,
			// and extra values in the rightNode-> The rightNode is the
			// destination for AppendProperties.

			assert leftForm.isArray();

			for (Iterator il = leftNode.iterateChildren(); il.hasNext();)
			{
				XMPNode leftItem = (XMPNode) il.next();

				boolean match = false;
				for (Iterator ir = rightNode.iterateChildren(); ir.hasNext();)
				{
					XMPNode rightItem = (XMPNode) ir.next();
					if (itemValuesMatch(leftItem, rightItem))
					{
						match = true;
						break;
					}
				}
				if (!match)
				{
					return false;
				}
			}
		}
		return true; // All of the checks passed.
	}

	public static void duplicateSubtree(XMPMeta source, XMPMeta dest, String sourceNS,
		String sourceRoot, String destNS, String destRoot, PropertyOptions options) throws XMPException{
		
		boolean fullSourceTree = false;
		boolean fullDestTree   = false;
		XMPPath sourcePath, destPath; 
		XMPNode sourceNode = null;
		XMPNode destNode = null;
		
		ParameterAsserts.assertNotNull(source);
		ParameterAsserts.assertSchemaNS(sourceNS);
		ParameterAsserts.assertSchemaNS(sourceRoot);
		ParameterAsserts.assertNotNull(dest);
		ParameterAsserts.assertNotNull(destNS);
		ParameterAsserts.assertNotNull(destRoot);
		
		if(destNS.length() == 0){
			destNS = sourceNS;
		}
		
		if(destRoot.length() == 0){
			destRoot = sourceRoot;
		}
		
		if(sourceNS.equals("*")){
			fullSourceTree = true;
		}
		
		if(destNS.equals("*")){
			fullDestTree = true;
		}
		
		if ( (source == dest) && (fullSourceTree | fullDestTree) ) {
			throw new XMPException("Can't duplicate tree onto itself", XMPError.BADPARAM);
		}
		if ( fullSourceTree & fullDestTree ) {
			throw new XMPException( "Use Clone for full tree to full tree", XMPError.BADPARAM);
		}
		if(fullSourceTree){
			destPath = XMPPathParser.expandXPath(destNS, destRoot);
			XMPMetaImpl destImpl = (XMPMetaImpl)dest;
			destNode = XMPNodeUtils.findNode(destImpl.getRoot(), destPath, false, null);
			if(destNode == null || !(destNode.getOptions().isStruct()))
				throw new XMPException("Destination must be an existing struct", XMPError.BADXPATH);
			
			if(destNode.hasChildren()){
				if((options != null) && ((options.getOptions() & PropertyOptions.DELETE_EXISTING) != 0)){
					destNode.removeChildren();
				}
				else{
					throw new XMPException("Destination must be an empty struct", XMPError.BADXPATH);
				}
			}
			
			XMPMetaImpl sourceImpl = (XMPMetaImpl)source;
			for ( int schemaNum = 1, schemaLim = sourceImpl.getRoot().getChildrenLength(); schemaNum <= schemaLim; ++schemaNum ) {

				 XMPNode  currSchema = sourceImpl.getRoot().getChild(schemaNum);

				for ( int propNum = 1, propLim = currSchema.getChildrenLength(); propNum <= propLim; ++propNum ) {
					sourceNode = currSchema.getChild(propNum);
					destNode.addChild((XMPNode)sourceNode.clone(false));
					
					/*XMP_Node * copyNode = new XMP_Node ( destNode, sourceNode->name, sourceNode->value, sourceNode->options );
					destNode->children.push_back ( copyNode );
					CloneOffspring ( sourceNode, copyNode );*/  //implemented above
				}

			}
			
		}
		else if(fullDestTree){
			
			// The source node must be an existing struct, copy all of the fields to the dest top level.

			XMPMetaImpl srcImpl = (XMPMetaImpl)source;
			XMPMetaImpl dstImpl = (XMPMetaImpl)dest;
			sourcePath = XMPPathParser.expandXPath ( sourceNS, sourceRoot );
			sourceNode = XMPNodeUtils.findNode ( srcImpl.getRoot() , sourcePath , false, null);

			if ( (sourceNode == null) || !( sourceNode.getOptions().isStruct()) ) {
				throw new XMPException("Source must be an existing struct", XMPError.BADXPATH);
			}
			
			destNode = dstImpl.getRoot();
			
			if (  destNode.hasChildren() ) {
				if((options != null) && ((options.getOptions() & PropertyOptions.DELETE_EXISTING) != 0)) {
					destNode.removeChildren();
				} else {
					throw new XMPException("Source must be an existing struct", XMPError.BADXPATH);
				}
			}
			
			String nsPrefix;
			
			for ( int fieldNum = 1, fieldLim = sourceNode.getChildrenLength(); fieldNum <= fieldLim; ++fieldNum ) {

				XMPNode currField = sourceNode.getChild(fieldNum);

				int colonPos = currField.getName().indexOf(':');
				if (  colonPos == -1 ) continue;
				nsPrefix = new String ( currField.getName().substring(0, colonPos+1));
				
				XMPSchemaRegistry nsRegister = XMPMetaFactory.getSchemaRegistry();
				String nsURI = nsRegister.getNamespaceURI(nsPrefix);
				
				if(nsURI == null){
					throw new XMPException("Source field namespace is not global", XMPError.BADSCHEMA);
				}
				
				XMPNode destSchema = XMPNodeUtils.findSchemaNode ( dstImpl.getRoot(), nsURI, true );
				if ( destSchema == null){
					throw new XMPException("Failed to find destination schema", XMPError.BADSCHEMA);
				}
				
				destSchema.addChild((XMPNode)currField.clone(false));
			}
		}
		else{
			sourcePath = XMPPathParser.expandXPath(sourceNS, sourceRoot);
			destPath = XMPPathParser.expandXPath(destNS, destRoot);
			XMPMetaImpl sourceImpl = (XMPMetaImpl)source;
			XMPMetaImpl destImpl = (XMPMetaImpl)dest;
			sourceNode = XMPNodeUtils.findNode(sourceImpl.getRoot(), sourcePath, false, null);
			if(sourceNode == null)
				throw new XMPException("Can't find source subtree", XMPError.BADXPATH);
			destNode = XMPNodeUtils.findNode(destImpl.getRoot(), destPath, false, null);
			if(destNode != null)
				throw new XMPException("Destination subtree must not exist", XMPError.BADXPATH);
			destNode = XMPNodeUtils.findNode(destImpl.getRoot(), destPath, true, null);
			if(destNode == null)
				throw new XMPException("Can't create destination root node", XMPError.BADXPATH);
			if ( source == dest ) {
				for ( XMPNode testNode = destNode; testNode != null; testNode = testNode.getParent() ) {
					if ( testNode == sourceNode ) {
						// *** delete the just-created dest root node
						throw new XMPException("Destination subtree is within the source subtree", XMPError.BADXPATH);
					}
				}
			}
			destNode.setValue(sourceNode.getValue());
			destNode.setOptions(sourceNode.getOptions());
			sourceNode.cloneSubtree(destNode, false);
		}
		
	}
	
	
	
	
	
	
	/**
	 * Make sure the separator is OK. It must be one semicolon surrounded by
	 * zero or more spaces. Any of the recognized semicolons or spaces are
	 * allowed.
	 *
	 * @param separator
	 * @throws XMPException
	 */
	private static void checkSeparator(String separator) throws XMPException
	{
		boolean haveSemicolon = false;
		for (int i = 0; i < separator.length(); i++)
		{
			int charKind = classifyCharacter(separator.charAt(i));
			if (charKind == UCK_SEMICOLON)
			{
				if (haveSemicolon)
				{
					throw new XMPException("Separator can have only one semicolon",
						XMPError.BADPARAM);
				}
				haveSemicolon = true;
			}
			else if (charKind != UCK_SPACE)
			{
				throw new XMPException("Separator can have only spaces and one semicolon",
						XMPError.BADPARAM);
			}
		}
		if (!haveSemicolon)
		{
			throw new XMPException("Separator must have one semicolon", XMPError.BADPARAM);
		}
	}


	/**
	 * Make sure the open and close quotes are a legitimate pair and return the
	 * correct closing quote or an exception.
	 *
	 * @param quotes
	 *            opened and closing quote in a string
	 * @param openQuote
	 *            the open quote
	 * @return Returns a corresponding closing quote.
	 * @throws XMPException
	 */
	private static char checkQuotes(String quotes, char openQuote) throws XMPException
	{
		char closeQuote;

		int charKind = classifyCharacter(openQuote);
		if (charKind != UCK_QUOTE)
		{
			throw new XMPException("Invalid quoting character", XMPError.BADPARAM);
		}

		if (quotes.length() == 1)
		{
			closeQuote = openQuote;
		}
		else
		{
			closeQuote = quotes.charAt(1);
			charKind = classifyCharacter(closeQuote);
			if (charKind != UCK_QUOTE)
			{
				throw new XMPException("Invalid quoting character", XMPError.BADPARAM);
			}
		}

		if (closeQuote != getClosingQuote(openQuote))
		{
			throw new XMPException("Mismatched quote pair", XMPError.BADPARAM);
		}
		return closeQuote;
	}


	/**
	 * Classifies the character into normal chars, spaces, semicola, quotes,
	 * control chars.
	 *
	 * @param ch
	 *            a char
	 * @return Return the character kind.
	 */
	private static int classifyCharacter(char ch)
	{
		if (SPACES.indexOf(ch) >= 0 || (0x2000 <= ch && ch <= 0x200B))
		{
			return UCK_SPACE;
		}
		else if (COMMAS.indexOf(ch) >= 0)
		{
			return UCK_COMMA;
		}
		else if (SEMICOLA.indexOf(ch) >= 0)
		{
			return UCK_SEMICOLON;
		}
		else if (QUOTES.indexOf(ch) >= 0 || (0x3008 <= ch && ch <= 0x300F)
				|| (0x2018 <= ch && ch <= 0x201F))
		{
			return UCK_QUOTE;
		}
		else if (ch < 0x0020 || CONTROLS.indexOf(ch) >= 0)
		{
			return UCK_CONTROL;
		}
		else
		{
			// Assume typical case.
			return UCK_NORMAL;
		}
	}


	/**
	 * @param openQuote
	 *            the open quote char
	 * @return Returns the matching closing quote for an open quote.
	 */
	private static char getClosingQuote(char openQuote)
	{
		switch (openQuote)
		{
		case 0x0022:
			return 0x0022; // ! U+0022 is both opening and closing.
//		Not interpreted as brackets anymore
//		case 0x005B:
//			return 0x005D;
		case 0x00AB:
			return 0x00BB; // ! U+00AB and U+00BB are reversible.
		case 0x00BB:
			return 0x00AB;
		case 0x2015:
			return 0x2015; // ! U+2015 is both opening and closing.
		case 0x2018:
			return 0x2019;
		case 0x201A:
			return 0x201B;
		case 0x201C:
			return 0x201D;
		case 0x201E:
			return 0x201F;
		case 0x2039:
			return 0x203A; // ! U+2039 and U+203A are reversible.
		case 0x203A:
			return 0x2039;
		case 0x3008:
			return 0x3009;
		case 0x300A:
			return 0x300B;
		case 0x300C:
			return 0x300D;
		case 0x300E:
			return 0x300F;
		case 0x301D:
			return 0x301F; // ! U+301E also closes U+301D.
		default:
			return 0;
		}
	}


	/**
	 * Add quotes to the item.
	 *
	 * @param item
	 *            the array item
	 * @param openQuote
	 *            the open quote character
	 * @param closeQuote
	 *            the closing quote character
	 * @param allowCommas
	 *            flag if commas are allowed
	 * @return Returns the value in quotes.
	 */
	private static String applyQuotes(String item, char openQuote, char closeQuote,
			boolean allowCommas)
	{
		if (item == null)
		{
			item = "";
		}

		boolean prevSpace = false;
		int charOffset;
		int charKind;

		// See if there are any separators in the value. Stop at the first
		// occurrance. This is a bit
		// tricky in order to make typical typing work conveniently. The purpose
		// of applying quotes
		// is to preserve the values when splitting them back apart. That is
		// CatenateContainerItems
		// and SeparateContainerItems must round trip properly. For the most
		// part we only look for
		// separators here. Internal quotes, as in -- Irving "Bud" Jones --
		// won't cause problems in
		// the separation. An initial quote will though, it will make the value
		// look quoted.

		int i;
		for (i = 0; i < item.length(); i++)
		{
			char ch = item.charAt(i);
			charKind = classifyCharacter(ch);
			if (i == 0 && charKind == UCK_QUOTE)
			{
				break;
			}

			if (charKind == UCK_SPACE)
			{
				// Multiple spaces are a separator.
				if (prevSpace)
				{
					break;
				}
				prevSpace = true;
			}
			else
			{
				prevSpace = false;
				if ((charKind == UCK_SEMICOLON || charKind == UCK_CONTROL)
						|| (charKind == UCK_COMMA && !allowCommas))
				{
					break;
				}
			}
		}


		if (i < item.length())
		{
			// Create a quoted copy, doubling any internal quotes that match the
			// outer ones. Internal quotes did not stop the "needs quoting"
			// search, but they do need
			// doubling. So we have to rescan the front of the string for
			// quotes. Handle the special
			// case of U+301D being closed by either U+301E or U+301F.

			StringBuffer newItem = new StringBuffer(item.length() + 2);
			int splitPoint;
			for (splitPoint = 0; splitPoint <= i; splitPoint++)
			{
				if (classifyCharacter(item.charAt(i)) == UCK_QUOTE)
				{
					break;
				}
			}

			// Copy the leading "normal" portion.
			newItem.append(openQuote).append(item.substring(0, splitPoint));

			for (charOffset = splitPoint; charOffset < item.length(); charOffset++)
			{
				newItem.append(item.charAt(charOffset));
				if (classifyCharacter(item.charAt(charOffset)) == UCK_QUOTE
						&& isSurroundingQuote(item.charAt(charOffset), openQuote, closeQuote))
				{
					newItem.append(item.charAt(charOffset));
				}
			}

			newItem.append(closeQuote);

			item = newItem.toString();
		}

		return item;
	}


	/**
	 * @param ch a character
	 * @param openQuote the opening quote char
	 * @param closeQuote the closing quote char
	 * @return Return it the character is a surrounding quote.
	 */
	private static boolean isSurroundingQuote(char ch, char openQuote, char closeQuote)
	{
		return ch == openQuote || isClosingingQuote(ch, openQuote, closeQuote);
	}


	/**
	 * @param ch a character
	 * @param openQuote the opening quote char
	 * @param closeQuote the closing quote char
	 * @return Returns true if the character is a closing quote.
	 */
	private static boolean isClosingingQuote(char ch, char openQuote, char closeQuote)
	{
		return ch == closeQuote || (openQuote == 0x301D && ch == 0x301E || ch == 0x301F);
	}



	/**
	 * U+0022 ASCII space<br>
	 * U+3000, ideographic space<br>
	 * U+303F, ideographic half fill space<br>
	 * U+2000..U+200B, en quad through zero width space
	 */
	private static final String SPACES = "\u0020\u3000\u303F";
	/**
	 * U+002C, ASCII comma<br>
	 * U+FF0C, full width comma<br>
	 * U+FF64, half width ideographic comma<br>
	 * U+FE50, small comma<br>
	 * U+FE51, small ideographic comma<br>
	 * U+3001, ideographic comma<br>
	 * U+060C, Arabic comma<br>
	 * U+055D, Armenian comma
	 */
	private static final String COMMAS = "\u002C\uFF0C\uFF64\uFE50\uFE51\u3001\u060C\u055D";
	/**
	 * U+003B, ASCII semicolon<br>
	 * U+FF1B, full width semicolon<br>
	 * U+FE54, small semicolon<br>
	 * U+061B, Arabic semicolon<br>
	 * U+037E, Greek "semicolon" (really a question mark)
	 */
	private static final String SEMICOLA = "\u003B\uFF1B\uFE54\u061B\u037E";
	/**
	 * U+0022 ASCII quote<br>
	 * The square brackets are not interpreted as quotes anymore (bug #2674672)
	 * (ASCII '[' (0x5B) and ']' (0x5D) are used as quotes in Chinese and
	 * Korean.)<br>
	 * U+00AB and U+00BB, guillemet quotes<br>
	 * U+3008..U+300F, various quotes.<br>
	 * U+301D..U+301F, double prime quotes.<br>
	 * U+2015, dash quote.<br>
	 * U+2018..U+201F, various quotes.<br>
	 * U+2039 and U+203A, guillemet quotes.
	 */
	private static final String QUOTES =
		"\"\u00AB\u00BB\u301D\u301E\u301F\u2015\u2039\u203A";
		// "\"\u005B\u005D\u00AB\u00BB\u301D\u301E\u301F\u2015\u2039\u203A";
	/**
	 * U+0000..U+001F ASCII controls<br>
	 * U+2028, line separator.<br>
	 * U+2029, paragraph separator.
	 */
	private static final String CONTROLS = "\u2028\u2029";
	
	/**
	 * Moves the specified Property from one Meta to another.
	 *
	 * @param stdXMP
	 *            Meta Object from where the property needs to move
	 * @param extXMP
	 *            Meta Object to where the property needs to move
	 * @param schemaURI
	 *           	Schema of the specified property
	 * @param propName
	 *           	Name of the property           
	 *            
	 * @return true in case of  success otherwise false.
	 * 
	 * @throws XMPException in case of failure
	 */
	
	static boolean moveOneProperty(XMPMetaImpl stdXMP, XMPMetaImpl extXMP, String schemaURI, String propName)
			throws XMPException {

		XMPNode propNode = null;

		XMPNode stdSchema = XMPNodeUtils.findSchemaNode(stdXMP.getRoot(), schemaURI, false);
		if (stdSchema != null) {
			propNode = XMPNodeUtils.findChildNode(stdSchema, propName, false);
		}
		if (propNode == null)
			return false;

		XMPNode extSchema = XMPNodeUtils.findSchemaNode(extXMP.getRoot(), schemaURI, true);

		propNode.setParent(extSchema);

		extSchema.setImplicit(false);
		extSchema.addChild(propNode);

		stdSchema.removeChild(propNode);

		if (stdSchema.hasChildren() == false) {
			XMPNode xmpTree = stdSchema.getParent();
			xmpTree.removeChild(stdSchema);
		}

		return true;
	}
	
	/**
	 * estimates the size of an xmp node
	 *
	 * @param xmpNode
	 *            XMP Node Object   
	 *            
	 * @return the estimated size of the node
	 * 
	 */
	static int estimateSizeForJPEG(XMPNode xmpNode) {
		int estSize = 0;
		int nameSize = xmpNode.getName().length();

		boolean includeName = (!xmpNode.getOptions().isArray());

		if (xmpNode.getOptions().isSimple()) {

			if (includeName)
				estSize += (nameSize + 3); // Assume attribute form.
			estSize += xmpNode.getValue().length();

		} else if (xmpNode.getOptions().isArray()) {

			// The form of the value portion is:
			// <rdf:Xyz><rdf:li>...</rdf:li>...</rdf:Xyx>
			if (includeName)
				estSize += (2 * nameSize + 5);
			int arraySize = xmpNode.getChildrenLength();
			estSize += 9 + 10; // The rdf:Xyz tags.
			estSize += arraySize * (8 + 9); // The rdf:li tags.
			for (int i = 1; i <= arraySize; ++i) {
				estSize += estimateSizeForJPEG(xmpNode.getChild(i));
			}
		} else {

			// The form is: <headTag
			// rdf:parseType="Resource">...fields...</tailTag>
			if (includeName)
				estSize += (2 * nameSize + 5);
			estSize += 25; // The rdf:parseType="Resource" attribute.
			int fieldCount = xmpNode.getChildrenLength();
			for (int i = 1; i <= fieldCount; ++i) {
				estSize += estimateSizeForJPEG(xmpNode.getChild(i));
			}

		}
		return estSize;
	}
	
	/**
	 * Utility function for placing objects in a Map. It behaves like a multi map.
	 *
	 * @param multiMap
	 *            A Map object which takes Integer as a key and list of list of String as value
	 * @param key
	 * 			  A Key for the map
	 * @param stringPair
	 * 			  A value for the map             
	 * 
	 */
	private static void putObjectsInMultiMap(Map<Integer, List<List<String>>> multiMap, Integer key,
			List<String> stringPair) {
		if (multiMap == null)
			return;
		List<List<String>> tempList = multiMap.get(key);
		if (tempList == null) {
			tempList = new ArrayList<List<String>>();
			multiMap.put(key, tempList);
		}
		tempList.add(stringPair);
	}
	
	/**
	 * Utility function for retrieving biggest entry in the multimap
	 * 
	 * @see estimateSizeForJPEG for size calculation
	 * 
	 * @param multiMap
	 *            A Map object which takes Integer as a key and list of list of String as value
	 * 
	 * @return  the list with the maximum size.      
	 * 
	 */
	private static List<String> getBiggestEntryInMultiMap(Map<Integer, List<List<String>>> multiMap) {
		if (multiMap == null || multiMap.isEmpty())
			return null;

		List<List<String>> myList = multiMap.get(((TreeMap) multiMap).lastKey());
		List<String> myList1 = myList.get(0);
		myList.remove(0);
		if (myList.isEmpty()) {
			multiMap.remove(((TreeMap) multiMap).lastKey());
		}
		return myList1;
	}
	
	/**
	 * Utility function for creating esimated size map for different properties of XMP Packet.
	 * 
	 * @see packageForJPEG
	 * 
	 * @param stdXMP
	 *            Meta Object whose property sizes needs to calculate.
	 *  
	 * @param propSizes
	 * 			  A treeMap Object which takes Integer as a key and list of list of String as values	                 
	 * 
	 */
	
	static void createEstimatedSizeMap(XMPMetaImpl stdXMP, Map<Integer, List<List<String>>> propSizes) {

		for (int s = stdXMP.getRoot().getChildrenLength(); s > 0; --s) {
			XMPNode stdSchema = stdXMP.getRoot().getChild(s);
			for (int p = stdSchema.getChildrenLength(); p > 0; --p) {
				XMPNode stdProp = stdSchema.getChild(p);
				if ((stdSchema.getName().equals(XMPConst.NS_XMP_NOTE))
						&& (stdProp.getName().equals("xmpNote:HasExtendedXMP")))
					continue; // ! Don't move xmpNote:HasExtendedXMP.

				int propSize = estimateSizeForJPEG(stdProp);
				List<String> namePair = new ArrayList<String>();
				namePair.add(stdSchema.getName());
				namePair.add(stdProp.getName());
				putObjectsInMultiMap(propSizes, propSize, namePair);
			}
		}
	}
	
	/**
	 * Utility function for moving the largest property from One XMP Packet to another.
	 * 
	 * @see moveOneProperty
	 * @see packageForJPEG
	 * 
	 * @param stdXMP
	 *            Meta Object from where property moves.
	 *	
	 * @param extXMP
	 *            Meta Object to where property moves.
	 * 
	 * @param propSizes
	 * 			 A treeMap Object which holds the estimated sizes of the property of stdXMP as a key and 
	 * 			 their string representation as map values.                
	 * 
	 */
	static int moveLargestProperty(XMPMetaImpl stdXMP, XMPMetaImpl extXMP, Map<Integer, List<List<String>>> propSizes)
			throws XMPException {
		assert (!propSizes.isEmpty());

		@SuppressWarnings("rawtypes")
		int propSize = (Integer) ((TreeMap) propSizes).lastKey();
		List<String> tempList = getBiggestEntryInMultiMap(propSizes);
		String schemaURI = tempList.get(0);
		String propName = tempList.get(1);

		boolean moved = moveOneProperty(stdXMP, extXMP, schemaURI, propName);
		assert (moved);
		return propSize;

	}
	
	/**
	 * creates XMP serializations appropriate for a JPEG file.
	 *
	 * The standard XMP in a JPEG file is limited to 64K bytes. This function
	 * serializes the XMP metadata in an XMP object into a string of RDF . If
	 * the data does not fit into the 64K byte limit, it creates a second packet
	 * string with the extended data.
	 *
	 * @param origXMPImpl
	 *            The XMP object containing the metadata.
	 *
	 * @param stdStr
	 *            A string object in which to return the full standard XMP
	 *            packet.
	 *
	 * @param extStr
	 *            A string object in which to return the serialized extended
	 *            XMP, empty if not needed.
	 *
	 * @param digestStr
	 *            A string object in which to return an MD5 digest of the
	 *            serialized extended XMP, empty if not needed.
	 * 
	 * @throws NoSuchAlgorithmException if fail to find algorithm for MD5
	 * @throws XMPException in case of internal error occurs.
	 *
	 */

	public static void packageForJPEG ( XMPMeta origXMPImpl,
			   StringBuilder stdStr,
			   StringBuilder extStr,
			   StringBuilder digestStr ) throws XMPException, NoSuchAlgorithmException
{
		XMPMetaImpl origXMP = (XMPMetaImpl) origXMPImpl;
		assert ( (stdStr != null) && (extStr != null) && (digestStr != null) );	// ! Enforced by wrapper.
	
		final int kStdXMPLimit = 65000 ;
		final String kPacketTrailer = "<?xpacket end=\"w\"?>";
		final int kTrailerLen = kPacketTrailer.length();
		
		String tempStr = null;
		XMPMetaImpl stdXMP = new XMPMetaImpl();
		XMPMetaImpl extXMP = new XMPMetaImpl();
		SerializeOptions keepItSmall = new SerializeOptions(SerializeOptions.USE_COMPACT_FORMAT);
		keepItSmall.setPadding(0);
		keepItSmall.setIndent("");
		keepItSmall.setBaseIndent(0);
		keepItSmall.setNewline(" ");
		
		
		// Try to serialize everything. Note that we're making internal calls to SerializeToBuffer, so
		// we'll be getting back the pointer and length for its internal string.
		
		
		tempStr = XMPMetaFactory.serializeToString(origXMP,keepItSmall);
		
		if ( tempStr.length() > kStdXMPLimit ) {
		
			// Couldn't fit everything, make a copy of the input XMP and make sure there is no xmp:Thumbnails property.
			
			stdXMP.getRoot().setOptions(origXMP.getRoot().getOptions()) ;
			stdXMP.getRoot().setName(origXMP.getRoot().getName()) ;
			stdXMP.getRoot().setValue(origXMP.getRoot().getValue()); 
			
			origXMP.getRoot().cloneSubtree(stdXMP.getRoot(), false); 
		
			if ( stdXMP.doesPropertyExist ( XMPConst.NS_XMP, "Thumbnails" ) ) {
				stdXMP.deleteProperty ( XMPConst.NS_XMP, "Thumbnails" );
				tempStr = XMPMetaFactory.serializeToString(stdXMP,keepItSmall);
			}
	
		}
	
	if ( tempStr.length() > kStdXMPLimit ) {
	
		// Still doesn't fit, move all of the Camera Raw namespace. Add a dummy value for xmpNote:HasExtendedXMP.
	
		stdXMP.setProperty ( XMPConst.NS_XMP_NOTE, "HasExtendedXMP", "123456789-123456789-123456789-12", 
				new PropertyOptions(PropertyOptions.NO_OPTIONS));
	
		
		XMPNode crSchema = XMPNodeUtils.findSchemaNode(stdXMP.getRoot(), XMPConst.NS_CAMERARAW, false);
		
		if ( crSchema != null ) {
			crSchema.setParent(extXMP.getRoot());
			extXMP.getRoot().addChild(crSchema);
			stdXMP.getRoot().removeChild(crSchema);
			
			tempStr = XMPMetaFactory.serializeToString(stdXMP,keepItSmall);
		}
	
	}
	
	if ( tempStr.length() > kStdXMPLimit ) {
	
		// Still doesn't fit, move photoshop:History.
	
		boolean moved = moveOneProperty ( stdXMP, extXMP, XMPConst.NS_PHOTOSHOP, "photoshop:History" );
	
		if ( moved ) {
			tempStr = XMPMetaFactory.serializeToString(stdXMP,keepItSmall);
		}
	
	}
	
	if ( tempStr.length() > kStdXMPLimit ) {
	
		// Still doesn't fit, move top level properties in order of estimated size. This is done by
		// creating a multi-map that maps the serialized size to the string pair for the schema URI
		// and top level property name. Since maps are inherently ordered, a reverse iteration of
		// the map can be done to move the largest things first. We use a double loop to keep going
		// until the serialization actually fits, in case the estimates are off.
	
		Map<Integer,List<List<String>>> propSizes = new TreeMap<Integer,List<List<String>>>();
		createEstimatedSizeMap ( stdXMP, propSizes );
		
		// Outer loop to make sure enough is actually moved.
	
		while ( (tempStr.length() > kStdXMPLimit) && (! propSizes.isEmpty()) ) {
	
			// Inner loop, move what seems to be enough according to the estimates.
	
			int tempLen = tempStr.length();
			while ( (tempLen > kStdXMPLimit) && (! propSizes.isEmpty()) ) {
	
				int propSize = moveLargestProperty ( stdXMP, extXMP, propSizes );
				assert ( propSize > 0 );
			
				if ( propSize > tempLen ) propSize = tempLen;	// ! Don't go negative.
				tempLen -= propSize;
	
			}
	
			// Reserialize the remaining standard XMP.
	
			tempStr = XMPMetaFactory.serializeToString(stdXMP,keepItSmall);	
		}
	
	}
	
	if ( tempStr.length() > kStdXMPLimit ) {
		// Still doesn't fit, throw an exception and let the client decide what to do.
		// ! This should never happen with the policy of moving any and all top level properties.
			throw new XMPException( "Can't reduce XMP enough for JPEG file", XMPError.INTERNALFAILURE );
	}
	
	// Set the static output strings.
	
	if ( extXMP.getRoot().getChildrenLength() == 0 ) {
	
		// Just have the standard XMP.
		stdStr.append(tempStr);
		
	
	} else {
	
		// Have extended XMP. Serialize it, compute the digest, reset xmpNote:HasExtendedXMP, and
		// reserialize the standard XMP.
	
		
		tempStr = XMPMetaFactory.serializeToString(extXMP,
				new SerializeOptions(SerializeOptions.USE_COMPACT_FORMAT | SerializeOptions.OMIT_PACKET_WRAPPER));
		
		extStr.append(tempStr);
		
		MessageDigest md = MessageDigest.getInstance("MD5");
	    md.update(tempStr.getBytes());
	    
	    byte byteData[] = md.digest();
	    
        for (int i = 0; i < byteData.length; i++) {
        	digestStr.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
	
        stdXMP.setProperty ( XMPConst.NS_XMP_NOTE, "HasExtendedXMP", digestStr.toString(), 
        		new PropertyOptions(PropertyOptions.NO_OPTIONS));
        tempStr = XMPMetaFactory.serializeToString(stdXMP,keepItSmall);
        stdStr.append(tempStr);
	}
	
		// Adjust the standard XMP padding to be up to 2KB.
		
		assert ( (stdStr.length() > kTrailerLen) && (stdStr.length() <= kStdXMPLimit) );		
		
		
		int extraPadding = kStdXMPLimit - stdStr.length();	// ! Do this before erasing the trailer.
		if ( extraPadding > 2047 ) extraPadding = 2047;
		stdStr.delete(stdStr.toString().indexOf(kPacketTrailer),stdStr.length());
		
		for(int i = 0; i < extraPadding; ++i) {
			stdStr.append(' ');
		}
		
		stdStr.append(kPacketTrailer).toString();	
	}
	
	/**
	 * merges standard and extended XMP retrieved from a JPEG file.
	 *
	 * When an extended partition stores properties that do not fit into the
	 * JPEG file limitation of 64K bytes, this function integrates those
	 * properties back into the same XMP object with those from the standard XMP
	 * packet.
	 *
	 * @param fullXMP
	 *            An XMP object which the caller has initialized from the
	 *            standard XMP packet in a JPEG file. The extended XMP is added
	 *            to this object.
	 *
	 * @param extendedXMP
	 *            An XMP object which the caller has initialized from the
	 *            extended XMP packet in a JPEG file.
	 *
	 * @throws XMPException
	 *             in case of internal error occurs.
	 */
	public static void mergeFromJPEG ( XMPMeta fullXMP,
	                          XMPMeta extendedXMP ) throws XMPException
	{

		TemplateOptions flags = new TemplateOptions(TemplateOptions.REPLACE_EXISTING_PROPERTIES |TemplateOptions.INCLUDE_INTERNAL_PROPERTIES);
		applyTemplate ( (XMPMetaImpl)fullXMP, (XMPMetaImpl)extendedXMP, flags );
		fullXMP.deleteProperty ( XMPConst.NS_XMP_NOTE, "HasExtendedXMP" );

	}
	
	
	/**
	 * modifies a working XMP object according to a template object.
	 *
	 * The XMP template can be used to add, replace or delete properties from
	 * the working XMP object. The actions that you specify determine how the
	 * template is applied. Each action can be applied individually or combined;
	 * if you do not specify any actions, the properties and values in the
	 * working XMP object do not change.
	 *
	 * @param OrigXMP
	 *            The destination XMP object.
	 *
	 * @param tempXMP
	 *            The template to apply to the destination XMP object.
	 *
	 * @param actions
	 *            Option flags to control the copying. If none are specified,
	 *            the properties and values in the working XMP do not change. A
	 *            logical OR of these bit-flag constants:
	 *            <ul>
	 *            <li><code> CLEAR_UNNAMED_PROPERTIES </code> Delete anything
	 *            that is not in the template.
	 *            <li><code> ADD_NEW_PROPERTIES </code> Add properties; see
	 *            detailed description.
	 *            <li><code> REPLACE_EXISTING_PROPERTIES </code> Replace the
	 *            values of existing properties.
	 *            <li><code> REPLACE_WITH_DELETE_EMPTY </code> Replace the
	 *            values of existing properties and delete properties if the new
	 *            value is empty.
	 *            <li><code> INCLUDE_INTERNAL_PROPERTIES </code> Operate on
	 *            internal properties as well as external properties.
	 *            </ul>
	 *
	 * @throws XMPException
	 *             in case of internal error occurs.
	 */
	
	static public void applyTemplate(XMPMeta OrigXMP, XMPMeta tempXMP, TemplateOptions actions) throws XMPException {

		XMPMetaImpl workingXMP = (XMPMetaImpl) OrigXMP;
		XMPMetaImpl templateXMP = (XMPMetaImpl) tempXMP;

		boolean doClear = (actions.getOptions() & TemplateOptions.CLEAR_UNNAMED_PROPERTIES) != 0;
		boolean doAdd = (actions.getOptions() & TemplateOptions.ADD_NEW_PROPERTIES) != 0;
		boolean doReplace = (actions.getOptions() & TemplateOptions.REPLACE_EXISTING_PROPERTIES) != 0;

		boolean deleteEmpty = (actions.getOptions() & TemplateOptions.REPLACE_WITH_DELETE_EMPTY) != 0;
		doReplace |= deleteEmpty; // Delete-empty implies Replace.
		deleteEmpty &= (!doClear); // Clear implies not delete-empty, but keep
									// the implicit Replace.

		boolean doAll = (actions.getOptions() & TemplateOptions.INCLUDE_INTERNAL_PROPERTIES) != 0;

		// ! In several places we do loops backwards so that deletions do not
		// perturb the remaining indices.
		// ! These loops use ordinals (size .. 1), we must use a zero based
		// index inside the loop.

		if (doClear) {

			// Visit the top level working properties, delete if not in the
			// template.

			for (int schemaOrdinal = workingXMP.getRoot().getChildrenLength(); schemaOrdinal > 0; --schemaOrdinal) {
				XMPNode workingSchema = workingXMP.getRoot().getChild(schemaOrdinal);
				XMPNode templateSchema = XMPNodeUtils.findSchemaNode(templateXMP.getRoot(), workingSchema.getName(),
						false);

				if (templateSchema == null) {

					// The schema is not in the template, delete all properties
					// or just all external ones.

					if (doAll) {
						workingSchema.removeChildren(); // Remove the properties here, delete the schema below.
					} else {
						for (int propOrdinal = workingSchema.getChildrenLength(); propOrdinal > 0; --propOrdinal) {
							XMPNode workingProp = workingSchema.getChild(propOrdinal);
							if (!Utils.isInternalProperty(workingSchema.getName(), workingProp.getName())) {
								workingSchema.removeChild(propOrdinal);
							}
						}
					}

				} else {
					// Check each of the working XMP's properties to see if it is in the template.
					for (int propOrdinal = workingSchema.getChildrenLength(); propOrdinal > 0; --propOrdinal) {
						XMPNode workingProp = workingSchema.getChild(propOrdinal);
						if ((doAll || !Utils.isInternalProperty(workingSchema.getName(), workingProp.getName()))
								&& (XMPNodeUtils.findChildNode(templateSchema, workingProp.getName(), false) == null)) {
							workingSchema.removeChild(propOrdinal);
						}
					}
				}
				if (workingSchema.hasChildren() == false) {
					workingXMP.getRoot().removeChild(schemaOrdinal);
				}
			}
		}

		if (doAdd | doReplace) {

			for (int schemaNum = 0, schemaLim = templateXMP.getRoot()
					.getChildrenLength(); schemaNum < schemaLim; ++schemaNum) {

				XMPNode templateSchema = templateXMP.getRoot().getChild(schemaNum + 1);

				// Make sure we have an output schema node, then process the top level template properties.

				XMPNode workingSchema = XMPNodeUtils.findSchemaNode(workingXMP.getRoot(), templateSchema.getName(),
						false);
				if (workingSchema == null) {
					workingSchema = new XMPNode(templateSchema.getName(), templateSchema.getValue(),
							new PropertyOptions(PropertyOptions.SCHEMA_NODE));
					workingXMP.getRoot().addChild(workingSchema);
					workingSchema.setParent(workingXMP.getRoot());
				}

				for (int propNum = 1, propLim = templateSchema.getChildrenLength(); propNum <= propLim; ++propNum) {
					XMPNode templateProp = templateSchema.getChild(propNum);
					if (doAll || !Utils.isInternalProperty(templateSchema.getName(), templateProp.getName())) {
						appendSubtree(workingXMP, templateProp, workingSchema, doAdd, doReplace, deleteEmpty);
					}
				}

				if (workingSchema.hasChildren() == false) {
					workingXMP.getRoot().removeChild(workingSchema);
				}

			}

		}
	}

}
