// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.adobe.internal.xmp.options;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.adobe.internal.xmp.XMPMetaFactory;


/**
 * Options for {@link XMPMetaFactory#parse(InputStream, ParseOptions)}.
 *
 * @author Stefan Makswit
 * @version $Revision$
 * @since 24.01.2006
 */
public final class ParseOptions extends Options
{
	/** Require a surrounding &quot;x:xmpmeta&quot; element in the xml-document. */
	public static final int REQUIRE_XMP_META = 0x0001;
	/** Do not reconcile alias differences, throw an exception instead. */
	public static final int STRICT_ALIASING = 0x0004;
	/** Convert ASCII control characters 0x01 - 0x1F (except tab, cr, and lf) to spaces. */
	public static final int FIX_CONTROL_CHARS = 0x0008;
	/** If the input is not unicode, try to parse it as ISO-8859-1. */
	public static final int ACCEPT_LATIN_1 = 0x0010;

	/** Do not carry run the XMPNormalizer on a packet, leave it as it is. */
	public static final int OMIT_NORMALIZATION = 0x0020;
	/** Disallow DOCTYPE declarations to prevent entity expansion attacks.
	 *  <em>Note:</em> Slight performance loss when set */
	public static final int DISALLOW_DOCTYPE = 0x0040;
	
	/** Map of nodes whose children are to be limited */
	private Map<String, Integer> mXMPNodesToLimit = new HashMap<String, Integer>();
	
	/**
	 * Sets the options to the default values.
	 */
	public ParseOptions()
	{
		setOption(FIX_CONTROL_CHARS | ACCEPT_LATIN_1 | DISALLOW_DOCTYPE, true);
	}


	/**
	 * @return Returns the requireXMPMeta.
	 */
	public boolean getRequireXMPMeta()
	{
		return getOption(REQUIRE_XMP_META);
	}


	/**
	 * @param value the value to set
	 * @return Returns the instance to call more set-methods.
	 */
	public ParseOptions setRequireXMPMeta(boolean value)
	{
		setOption(REQUIRE_XMP_META, value);
		return this;
	}


	/**
	 * @return Returns the strictAliasing.
	 */
	public boolean getStrictAliasing()
	{
		return getOption(STRICT_ALIASING);
	}


	/**
	 * @param value the value to set
	 * @return Returns the instance to call more set-methods.
	 */
	public ParseOptions setStrictAliasing(boolean value)
	{
		setOption(STRICT_ALIASING, value);
		return this;
	}


	/**
	 * @return Returns the strictAliasing.
	 */
	public boolean getFixControlChars()
	{
		return getOption(FIX_CONTROL_CHARS);
	}


	/**
	 * @param value the value to set
	 * @return Returns the instance to call more set-methods.
	 */
	public ParseOptions setFixControlChars(boolean value)
	{
		setOption(FIX_CONTROL_CHARS, value);
		return this;
	}


	/**
	 * @return Returns the strictAliasing.
	 */
	public boolean getAcceptLatin1()
	{
		return getOption(ACCEPT_LATIN_1);
	}


	/**
	 * @param value the value to set
	 * @return Returns the instance to call more set-methods.
	 */
	public ParseOptions setOmitNormalization(boolean value)
	{
		setOption(OMIT_NORMALIZATION, value);
		return this;
	}


	/**
	 * @return Returns the option "omit normalization".
	 */
	public boolean getOmitNormalization()
	{
		return getOption(OMIT_NORMALIZATION);
	}


	/**
	 * @param value the value to set
	 * @return Returns the instance to call more set-methods.
	 */
	public ParseOptions setDisallowDoctype(boolean value)
	{
		setOption(DISALLOW_DOCTYPE, value);
		return this;
	}


	/**
	 * @return Returns the option "disallow DOCTYPE".
	 */
	public boolean getDisallowDoctype()
	{
		return getOption(DISALLOW_DOCTYPE);
	}


	/**
	 * @param value the value to set
	 * @return Returns the instance to call more set-methods.
	 */
	public ParseOptions setAcceptLatin1(boolean value)
	{
		setOption(ACCEPT_LATIN_1, value);
		return this;
	}


	/**
	 * @return Returns true if some XMP nodes have been limited.
	 */
	public boolean areXMPNodesLimited()
	{
		return ((mXMPNodesToLimit.size() > 0));
	}


	/**
	 * @param nodeMap the Map with name of nodes and number-of-items to limit them to
	 * @return Returns the instance to call more set-methods.
	 */
	public ParseOptions setXMPNodesToLimit(Map<String, Integer> nodeMap)
	{
		mXMPNodesToLimit.putAll(nodeMap);
		return this;
	}


	/**
	 * @return Returns Unmodifiable Map containing names oF XMP nodes to limit and number-of-items limit corresponding to the XMP nodes.
	 */
	public Map<String, Integer> getXMPNodesToLimit()
	{
		return Collections.unmodifiableMap(mXMPNodesToLimit);
	}


	/**
	 * @see Options#defineOptionName(int)
	 */
	protected String defineOptionName(int option)
	{
		switch (option)
		{
			case REQUIRE_XMP_META :		return "REQUIRE_XMP_META";
			case STRICT_ALIASING :		return "STRICT_ALIASING";
			case FIX_CONTROL_CHARS:		return "FIX_CONTROL_CHARS";
			case ACCEPT_LATIN_1:		return "ACCEPT_LATIN_1";
			case OMIT_NORMALIZATION:	return "OMIT_NORMALIZATION";
			case DISALLOW_DOCTYPE:		return "DISALLOW_DOCTYPE";
			default: 					return null;
		}
	}


	/**
	 * @see Options#getValidOptions()
	 */
	protected int getValidOptions()
	{
		return
			REQUIRE_XMP_META |
			STRICT_ALIASING |
			FIX_CONTROL_CHARS |
			ACCEPT_LATIN_1 |
			OMIT_NORMALIZATION |
			DISALLOW_DOCTYPE;
	}
}
