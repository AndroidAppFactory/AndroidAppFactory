// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.adobe.internal.xmp.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.adobe.internal.xmp.options.SerializeOptions;


/**
 * Factory for XMLStreamWriter
 *
 * @author  Stefan Makswit
 * @version $Revision$
 * @since   07.11.2006
 */
public class XMLStreamWriterFactory
{
	/**
	 * Creates an XMLStreamWriterImpl on top of standard writer with XMP serialization options.
	 * @param writer a <code>Writer</code>
	 * @param options serialization options
	 * @return Returns an XML stream writer.
	 */
	public static XMLStreamWriterImpl create(Writer writer, SerializeOptions options)
	{
		return new XMLStreamWriterImpl(writer, options);
	}


	/**
	 * Creates an XMLStreamWriterImpl on top of an output stream with XMP serialization options.
	 * @param stream an <code>OutputStream</code>
	 * @param options serialization options
	 * @return Returns an XML stream writer.
	 * @throws IOException If the encoding provided by <code>SerializeOptions</code> is not supported
	 */
	public static XMLStreamWriterImpl create(OutputStream stream, SerializeOptions options)
			throws IOException
	{
		try
		{
			return create(new BufferedWriter(new OutputStreamWriter(stream, options.getEncoding()),
				4096), options);
		}
		catch (java.io.UnsupportedEncodingException uee)
		{
			throw new IOException("Unsupported encoding " + options.getEncoding());
		}
	}
}