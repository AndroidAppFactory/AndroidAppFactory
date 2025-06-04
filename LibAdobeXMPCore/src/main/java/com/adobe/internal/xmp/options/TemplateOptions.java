// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================
package com.adobe.internal.xmp.options;

import com.adobe.internal.xmp.XMPException;

/**
 * Options for XMPSchemaRegistryImpl#registerAlias.
 *
 * @author Anuj Gupta
 * @version $Revision$
 * @since 02.06.2016
 */

public final class TemplateOptions extends Options{
	
	/* Options used by apply Template functions */
	public static final int CLEAR_UNNAMED_PROPERTIES = 0x00000002;
	
	public static final int REPLACE_EXISTING_PROPERTIES = 0x00000010;
	
	public static final int INCLUDE_INTERNAL_PROPERTIES = 0x00000020;
	
	public static final int ADD_NEW_PROPERTIES = 0x00000040;
	
	public static final int REPLACE_WITH_DELETE_EMPTY = 0x00000080;
	
	public TemplateOptions()
	{
		// reveal default constructor
	}


	/**
	 * Intialization constructor
	 *
	 * @param options the initialization options
	 * @throws XMPException If the options are not valid
	 */
	public TemplateOptions(int options) throws XMPException
	{
		super(options);
	}

	@Override
	/**
	 * @see Options#getValidOptions()
	 */
	protected int getValidOptions() {
		return CLEAR_UNNAMED_PROPERTIES | REPLACE_EXISTING_PROPERTIES | INCLUDE_INTERNAL_PROPERTIES | ADD_NEW_PROPERTIES
				| REPLACE_WITH_DELETE_EMPTY;
	}

	@Override
	/**
	 * @see Options#defineOptionName(int)
	 */
	protected String defineOptionName(int option) {
		switch (option)
		{
			case CLEAR_UNNAMED_PROPERTIES : 	return "CLEAR_UNNAMED_PROPERTIES";
			case REPLACE_EXISTING_PROPERTIES : 	return "REPLACE_EXISTING_PROPERTIES";
			case INCLUDE_INTERNAL_PROPERTIES : 	return "INCLUDE_INTERNAL_PROPERTIES";
			case ADD_NEW_PROPERTIES : 			return "ADD_NEW_PROPERTIES";
			case REPLACE_WITH_DELETE_EMPTY : 	return "REPLACE_WITH_DELETE_EMPTY";
			default: 							return null;
		}
	}
}
