// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.adobe.internal.xmp.properties;

import com.adobe.internal.xmp.XMPMeta;
import com.adobe.internal.xmp.options.PropertyOptions;


/**
 * This interface is used to return a text property together with its and options.
 *
 * @author  Stefan Makswit
 * @version $Revision$
 * @since   23.01.2006
 */
public interface XMPProperty
{
	/**
	 * @return Returns the value of the property.
	 */
	String getValue();


	/**
	 * @return Returns the options of the property.
	 */
	PropertyOptions getOptions();


	/**
	 * Only set by {@link XMPMeta#getLocalizedText(String, String, String, String)}.
	 * @return Returns the language of the alt-text item.
	 */
	String getLanguage();
}
