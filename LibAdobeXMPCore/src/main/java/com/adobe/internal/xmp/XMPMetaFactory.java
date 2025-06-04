//=================================================================================================
//ADOBE SYSTEMS INCORPORATED
//Copyright 2006 Adobe Systems Incorporated
//All Rights Reserved
//
//NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
//of the Adobe license agreement accompanying it.
//=================================================================================================

package com.adobe.internal.xmp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adobe.internal.xmp.impl.XMPMetaImpl;
import com.adobe.internal.xmp.impl.XMPMetaParser;
import com.adobe.internal.xmp.impl.XMPSchemaRegistryImpl;
import com.adobe.internal.xmp.impl.XMPSerializerHelper;
import com.adobe.internal.xmp.options.ParseOptions;
import com.adobe.internal.xmp.options.SerializeOptions;


/**
 * Creates <code>XMPMeta</code>-instances from an <code>InputStream</code>
 *
 * @author Stefan Makswit
 * @version $Revision$
 * @since 30.01.2006
 */
public final class XMPMetaFactory
{
	/** The singleton instance of the <code>XMPSchemaRegistry</code>. */
	private static XMPSchemaRegistry schema = new XMPSchemaRegistryImpl();
	/** cache for version info */
	private static XMPVersionInfo versionInfo = null;

	/**
	 * Hides public constructor
	 */
	private XMPMetaFactory()
	{
		// EMPTY
	}


	/**
	 * @return Returns the singleton instance of the <code>XMPSchemaRegistry</code>.
	 */
	public static XMPSchemaRegistry getSchemaRegistry()
	{
		return schema;
	}


	/**
	 * @return Returns an empty <code>XMPMeta</code>-object.
	 */
	public static XMPMeta create()
	{
		return new XMPMetaImpl();
	}


	/**
	 * Parsing with default options.
	 * @see XMPMetaFactory#parse(InputStream, ParseOptions)
	 *
	 * @param in an <code>InputStream</code>
	 * @return Returns the <code>XMPMeta</code>-object created from the input.
	 * @throws XMPException If the file is not well-formed XML or if the parsing fails.
	 */
	public static XMPMeta parse(InputStream in) throws XMPException
	{
		return parse(in, null);
	}


	/**
	 * These functions support parsing serialized RDF into an XMP object, and serailizing an XMP
	 * object into RDF. The input for parsing may be any valid Unicode
	 * encoding. ISO Latin-1 is also recognized, but its use is strongly discouraged. Serialization
	 * is always as UTF-8.
	 * <p>
	 * <code>parseFromBuffer()</code> parses RDF from an <code>InputStream</code>. The encoding
	 * is recognized automatically.
	 *
	 * @param in an <code>InputStream</code>
	 * @param options Options controlling the parsing.<br>
	 *        The available options are:
	 *        <ul>
	 *        <li> XMP_REQUIRE_XMPMETA - The &lt;x:xmpmeta&gt; XML element is required around
	 *        <tt>&lt;rdf:RDF&gt;</tt>.
	 *        <li> XMP_STRICT_ALIASING - Do not reconcile alias differences, throw an exception.
	 *        </ul>
	 *        <em>Note:</em>The XMP_STRICT_ALIASING option is not yet implemented.
	 * @return Returns the <code>XMPMeta</code>-object created from the input.
	 * @throws XMPException If the file is not well-formed XML or if the parsing fails.
	 */
	public static XMPMeta parse(InputStream in, ParseOptions options)
			throws XMPException
	{
		return XMPMetaParser.parse(in, options);
	}


	/**
	 * Parsing with default options.
	 * @see XMPMetaFactory#parse(InputStream)
	 *
	 * @param packet a String contain an XMP-file.
	 * @return Returns the <code>XMPMeta</code>-object created from the input.
	 * @throws XMPException If the file is not well-formed XML or if the parsing fails.
	 */
	public static XMPMeta parseFromString(String packet) throws XMPException
	{
		return parseFromString(packet, null);
	}


	/**
	 * Creates an <code>XMPMeta</code>-object from a string.
	 * @see XMPMetaFactory#parseFromString(String, ParseOptions)
	 *
	 * @param packet a String contain an XMP-file.
	 * @param options Options controlling the parsing.
	 * @return Returns the <code>XMPMeta</code>-object created from the input.
	 * @throws XMPException If the file is not well-formed XML or if the parsing fails.
	 */
	public static XMPMeta parseFromString(String packet, ParseOptions options)
			throws XMPException
	{
		return XMPMetaParser.parse(packet, options);
	}


	/**
	 * Parsing with default options.
	 * @see XMPMetaFactory#parseFromBuffer(byte[], ParseOptions)
	 *
	 * @param buffer a String contain an XMP-file.
	 * @return Returns the <code>XMPMeta</code>-object created from the input.
	 * @throws XMPException If the file is not well-formed XML or if the parsing fails.
	 */
	public static XMPMeta parseFromBuffer(byte[] buffer) throws XMPException
	{
		return parseFromBuffer(buffer, null);
	}


	/**
	 * Creates an <code>XMPMeta</code>-object from a byte-buffer.
	 * @see XMPMetaFactory#parse(InputStream, ParseOptions)
	 *
	 * @param buffer a String contain an XMP-file.
	 * @param options Options controlling the parsing.
	 * @return Returns the <code>XMPMeta</code>-object created from the input.
	 * @throws XMPException If the file is not well-formed XML or if the parsing fails.
	 */
	public static XMPMeta parseFromBuffer(byte[] buffer,
		ParseOptions options) throws XMPException
	{
		return XMPMetaParser.parse(buffer, options);
	}


	/**
	 * Serializes an <code>XMPMeta</code>-object as RDF into an <code>OutputStream</code>
	 * with default options.
	 *
	 * @param xmp a metadata object
	 * @param out an <code>OutputStream</code> to write the serialized RDF to.
	 * @throws XMPException on serializsation errors.
	 */
	public static void serialize(XMPMeta xmp, OutputStream out) throws XMPException
	{
		serialize(xmp, out, null);
	}


	/**
	 * Serializes an <code>XMPMeta</code>-object as RDF into an <code>OutputStream</code>.
	 *
	 * @param xmp a metadata object
	 * @param options Options to control the serialization (see {@link SerializeOptions}).
	 * @param out an <code>OutputStream</code> to write the serialized RDF to.
	 * @throws XMPException on serializsation errors.
	 */
	public static void serialize(XMPMeta xmp, OutputStream out, SerializeOptions options)
			throws XMPException
	{
		assertImplementation(xmp);
		XMPSerializerHelper.serialize((XMPMetaImpl) xmp, out, options);
	}


	/**
	 * Serializes an <code>XMPMeta</code>-object as RDF into a byte buffer.
	 *
	 * @param xmp a metadata object
	 * @param options Options to control the serialization (see {@link SerializeOptions}).
	 * @return Returns a byte buffer containing the serialized RDF.
	 * @throws XMPException on serializsation errors.
	 */
	public static byte[] serializeToBuffer(XMPMeta xmp, SerializeOptions options)
			throws XMPException
	{
		assertImplementation(xmp);
		return XMPSerializerHelper.serializeToBuffer((XMPMetaImpl) xmp, options);
	}


	/**
	 * Serializes an <code>XMPMeta</code>-object as RDF into a string. <em>Note:</em> Encoding
	 * is ignored when serializing to a string.
	 *
	 * @param xmp a metadata object
	 * @param options Options to control the serialization (see {@link SerializeOptions}).
	 * @return Returns a string containing the serialized RDF.
	 * @throws XMPException on serializsation errors.
	 */
	public static String serializeToString(XMPMeta xmp, SerializeOptions options)
			throws XMPException
	{
		assertImplementation(xmp);
		return XMPSerializerHelper.serializeToString((XMPMetaImpl) xmp, options);
	}


	/**
	 * @param xmp Asserts that xmp is compatible to <code>XMPMetaImpl</code>.s
	 */
	private static void assertImplementation(XMPMeta xmp)
	{
		if (!(xmp instanceof XMPMetaImpl))
		{
			throw new UnsupportedOperationException("The serializing service works only" +
				"with the XMPMeta implementation of this library");
		}
	}


	/**
	 * Resets the schema registry to its original state (creates a new one).
	 * Be careful this might break all existing XMPMeta-objects and should be used
	 * only for testing purpurses.
	 */
	public static void reset()
	{
		schema = new XMPSchemaRegistryImpl();
	}


	/**
	 * Obtain version information. The XMPVersionInfo singleton is created the first time
	 * its requested.
	 *
	 * @return Returns the version information.
	 */
	public static synchronized XMPVersionInfo getVersionInfo()
	{
		if (versionInfo == null)
		{
			// Initialize with test version
			String bundleVersion = "Test.SNAPSHOT";
			int major= 5;
			int minor = 0;
			int micro = 0;

			try
			{
				Enumeration<URL> resources = XMPMetaFactory.class
						.getClassLoader().getResources("META-INF/MANIFEST.MF");

				while (resources.hasMoreElements())
				{
					Manifest manifest = new Manifest(resources.nextElement()
							.openStream());
					Attributes attrs = manifest.getMainAttributes();
					// find the correct manifest
					if ("com.adobe.xmp.xmpcore".equals(attrs
							.getValue("Bundle-SymbolicName")))
					{
						if (attrs.getValue("Bundle-Version") != null)
						{
							bundleVersion = attrs.getValue("Bundle-Version");
							Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+).*");
							Matcher matcher = pattern.matcher(bundleVersion);
							if (matcher.find())
							{
								major = Integer.parseInt(matcher.group(1));
								minor = Integer.parseInt(matcher.group(2));
								micro = Integer.parseInt(matcher.group(3));
								break;
							}
						}
					}
				}
			}
			catch (IOException E)
			{
				// EMPTY
			}

			// Adobe XMP Core 5.0.19.SNAPSHOT
			final String message = "Adobe XMP Core " + bundleVersion;
			final int majorVersion = major;
			final int minorVersion = minor;
			final int microVersion = micro;

			versionInfo = new XMPVersionInfo()
			{
				public int getMajor()
				{
					return majorVersion;
				}

				public int getMinor()
				{
					return minorVersion;
				}

				public int getMicro()
				{
					return microVersion;
				}

				public boolean isDebug()
				{
					return true;
				}

				public int getBuild()
				{
					return 0;
				}

				public String getMessage()
				{
					return message;
				}

				public String toString()
				{
					return message;
				}
			};

		}
		return versionInfo;
	}
}
