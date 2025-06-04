// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.adobe.internal.xmp.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Stack;

import com.adobe.internal.xmp.impl.Utils;
import com.adobe.internal.xmp.options.SerializeOptions;


/**
 * Uncomplete implementation of XMPStreamWriter of the upcoming XML Streaming API,
 * that is adapted to be used for XMPCore.
 * Adds automatically correct closing of empty tags &gt; and pretty printing.
 * Simplifies namespace handling; just tracks if a certain prefix has already been registered.
 * FfF: If StAX will become part of the JRE, it might be replaced.
 *
 * @author Stefan Makswit
 * @version $Revision$
 * @since 09.11.2006
 */
public class XMLStreamWriterImpl
{
	/** */
	private static final String DEFAULTNS = "";
	/** */
	private Writer writer;
	/** */
	private boolean startElementOpened = false;
	/** */
	private boolean emptyElement = false;
	/** these two stacks are used to implement the writeEndElement() method */
	private Stack qNameStack = new Stack();

	/** */
	private char[] newLineStr = new char[] { '\r' };
	/** */
	private int baseIndent = 0;
	/** */
	private char[] indentStr = new char[] { ' ', ' ' };
	/** */
	private int indentLevel = 0;
	/** Flag if chars enclosed in a tag are indented like body tags. */
	private boolean charIndent = false;
	/** flag if namespaces should start in a new line */
	private boolean namespaceLF = true;

	/** internal flag tracking the linefeeds */
	private boolean preventWhitespace = false;
	/** internal flag tracking the linefeeds */
	private boolean preventNextLF = true;
	/** stores the already registiered namspaces */
	private final HashSet registeredPrefixes = new HashSet();
	/** whitespaces (linefeeds) shall be escaped */
	private boolean escapeWhitespaces = true;


	/**
	 * Default constructor providing a writer the XML is written to
	 * and the serializsation options to determine indents, whitespaces and lineendings.
	 *
	 * @param writer a <code>Writer</code>
	 * @param options the serialization options
	 */
	public XMLStreamWriterImpl(Writer writer, SerializeOptions options)
	{
		this(writer);
		this.newLineStr = options.getNewline().toCharArray();
		this.indentStr = options.getIndent().toCharArray();
		this.baseIndent = options.getBaseIndent();
	}


	/**
	 * Default constructor providing a writer the XML is written to.
	 * @param writer a <code>Writer</code>
	 */
	public XMLStreamWriterImpl(Writer writer)
	{
		this.writer = writer;
	}


	/**
	 * Writes a start tag.
	 * @param qname a QName
	 * @throws IOException If an I/O error occurs
	 */

	public void writeStartElement(String qname) throws IOException
	{
		if (qname == null)
		{
			throw new IllegalArgumentException("The element name may not be null");
		}

		closeStartElement();

		writeNewLine();
		write("<");
		write(qname);
		startElementOpened = true;
		this.qNameStack.push(qname);
	}


	/**
	 * Differs from writeStartElement in that writeEndElement needs and must not be called.
	 * @param qname a QName
	 * @throws IOException If an I/O error occurs
	 */
	public void writeEmptyElement(String qname) throws IOException
	{
		writeStartElement(qname);
		emptyElement = true;
	}


	/**
	 * Writes a closing tag if the tag in scope has body elements or &gt; if its
	 * empty.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	public void writeEndElement() throws IOException
	{
		if (startElementOpened)
		{
			qNameStack.pop();
			write("/>");
			startElementOpened = false;

			if (emptyElement)
			{
				writeCloseElement();
				emptyElement = false;
			}
		}
		else
		{
			writeCloseElement();
		}
	}


	/**
	 * Closes the writer.
	 * @throws IOException If an I/O error occurs
	 */
	public void close() throws IOException
	{
		flush();
		writer.close();
	}


	/**
	 * Calls flush() on the writer.
	 * @throws IOException If an I/O error occurs
	 */
	public void flush() throws IOException
	{
		this.writer.flush();
	}


	/**
	 * Completes the document by writing the end tags for all open tags.
	 * @throws IOException If an I/O error occurs
	 */
	public void writeEndDocument() throws IOException
	{
		while (!this.qNameStack.isEmpty())
		{
			writeEndElement();
		}
	}


	/**
	 * Writes an attribute into an open tag.
	 * @param qname a QName
	 * @param value the attribute value
	 * @throws IOException If an I/O error occurs
	 */
	public void writeAttribute(String qname, String value) throws IOException
	{
		if (!this.startElementOpened)
		{
			throw new IOException("A start element must be written before an attribute");
		}
		// prepareNamespace(namespaceURI);
		write(" ");
		write(qname);
		write("=\"");
		writeCharactersInternal(value.toCharArray(), 0, value.length(), true);
		write("\"");
	}


	/**
	 * Writes a namespace attribute and tracks if the namespace is already defined.
	 * @param prefix the namespace prefix.
	 * @param namespaceURI the namespace URI
	 * @throws IOException If an I/O error occurs
	 */
	public void writeNamespace(String prefix, String namespaceURI) throws IOException
	{
		if (!this.startElementOpened)
		{
			throw new IOException("A start element must be written before a namespace");
		}
		if (prefix == null || "".equals(prefix) || "xmlns".equals(prefix))
		{
			writeDefaultNamespace(namespaceURI);
			return;
		}
		if (needToWriteNamespace(prefix))
		{
			if (namespaceLF)
			{
				indentLevel++;
				writeNewLine();
				indentLevel--;
			}
			else
			{
				write(' ');
			}
			write("xmlns:");
			write(prefix);
			write("=\"");
			write(namespaceURI);
			write("\"");
		}
	}


	/**
	 * Writes the default namespace
	 * @param namespaceURI the default namespace
	 * @throws IOException If an I/O error occurs
	 */
	public void writeDefaultNamespace(String namespaceURI) throws IOException
	{
		if (!this.startElementOpened)
		{
			throw new IOException("A start element must be written before the default namespace");
		}
		if (needToWriteNamespace(DEFAULTNS))
		{
			write(" xmlns");
			write("=\"");
			write(namespaceURI);
			write("\"");
			// setPrefix(DEFAULTNS, namespaceURI);
		}
	}


	/**
	 * Writes a comment.
	 * @param comment the comment to write
	 * @throws IOException If an I/O error occurs
	 */
	public void writeComment(String comment) throws IOException
	{
		closeStartElement();
		write("<!--");
		if (comment != null)
		{
			write(comment);
		}
		write("-->");
	}


	/**
	 * Writes a processing instruction.
	 * @param target a target
	 * @throws IOException If an I/O error occurs
	 */
	public void writeProcessingInstruction(String target) throws IOException
	{
		closeStartElement();
		writeProcessingInstruction(target, null);
	}


	/**
	 * Writes a processing instruction.
	 * @param target a target
	 * @param text the instruction text
	 * @throws IOException If an I/O error occurs
	 */
	public void writeProcessingInstruction(String target, String text) throws IOException
	{
		closeStartElement();
		writeNewLine();
		write("<?");
		if (target != null)
		{ // isn't passing null an error, actually?
			write(target);
		}
		if (text != null)
		{
			// Need a separating white space
			write(' ');
			write(text);
		}
		write("?>");
	}


	/**
	 * Writes the DTD.
	 * @param dtd the dtd to write
	 * @throws IOException If an I/O error occurs
	 */
	public void writeDTD(String dtd) throws IOException
	{
		writeNewLine();
		write(dtd);
	}


	/**
	 * Writes CData.
	 * @param data the data to write
	 * @throws IOException If an I/O error occurs
	 */
	public void writeCData(String data) throws IOException
	{
		closeStartElement();
		write("<![CDATA[");
		if (data != null)
		{
			write(data);
		}
		write("]]>");
	}


	/**
	 * Writes an entity reference.
	 * @param name the name of the reference
	 * @throws IOException If an I/O error occurs
	 */
	public void writeEntityRef(String name) throws IOException
	{
		closeStartElement();
		write("&");
		write(name);
		write(";");
	}


	/**
	 * Writes the XML-header and version.
	 * @throws IOException If an I/O error occurs
	 */
	public void writeStartDocument() throws IOException
	{
		writeNewLine();
		write("<?xml version='1.0' encoding='utf-8'?>");
	}


	/**
	 * Writes the XML-header and version.
	 * @param version th xml-version
	 * @throws IOException If an I/O error occurs
	 */
	public void writeStartDocument(String version) throws IOException
	{
		writeNewLine();
		write("<?xml version='");
		write(version);
		write("'?>");
	}


	/**
	 * Writes the XML-header, encoding and version.
	 * @param encoding the file encoding
	 * @param version th XML-version
	 * @throws IOException If an I/O error occurs
	 */
	public void writeStartDocument(String encoding, String version) throws IOException
	{
		writeNewLine();
		write("<?xml version='");
		write(version);
		write("' encoding='");
		write(encoding);
		write("'?>");
	}


	/**
	 * Writes XML-characters.
	 * @param text xml characters
	 * @throws IOException If an I/O error occurs
	 */
	public void writeCharacters(String text) throws IOException
	{
		writeCharacters(text.toCharArray(), 0, text.length());
	}


	/**
	 * Writes a part of a char array.
	 * @param buffer a character array
	 * @param off the offset within the array
	 * @param length the amount of character to write
	 * @throws IOException If an I/O error occurs
	 */
	public void writeCharacters(char[] buffer, int off, int length) throws IOException
	{
		boolean openStartElement = startElementOpened;
		closeStartElement();

		if (openStartElement)
		{
			if (charIndent)
			{
				writeNewLine();
			}
			else
			{
				preventWhitespace = true;
			}
		}

		writeCharactersInternal(buffer, off, length, false);
	}


	/**
	 * @param escapeWhitespaces the escapeWhitespaces to set
	 */
	public void setEscapeWhitespaces(boolean escapeWhitespaces)
	{
		this.escapeWhitespaces = escapeWhitespaces;
	}


	/**
	 * Writes a string.
	 * @param str a String
	 * @throws IOException If an I/O error occurs
	 */
	private void write(String str) throws IOException
	{
		this.writer.write(str);
	}


	/**
	 * Writes a char
	 * @param ch a char
	 * @throws IOException
	 */
	private void write(char ch) throws IOException
	{
		this.writer.write(ch);
	}


	/**
	 * Writes a char array.
	 * @param buffer a char array
	 * @throws IOException
	 */
	private void write(char[] buffer) throws IOException
	{
		this.writer.write(buffer);
	}


	/**
	 * Writes XML characters to the writer.
	 * @param buffer a character array
	 * @param off the offset
	 * @param length the amount of characters to write
	 * @param isAttributeValue flag if it is an attribute value (needed for escaping)
	 * @throws IOException
	 */
	private void writeCharactersInternal(char[] buffer, int off, int length,
			boolean isAttributeValue) throws IOException
	{
		indentLevel++;

		String value = Utils.escapeXML(new String(buffer, off, length),
				isAttributeValue, escapeWhitespaces);
		writer.write(value);

		indentLevel--;
	}


	/**
	 * Closes an open tag, either with an end tag or with "/>".
	 * @throws IOException
	 */
	private void closeStartElement() throws IOException
	{
		if (this.startElementOpened)
		{
			if (!emptyElement)
			{
				write(">");
			}
			else
			{
				qNameStack.pop();
				write("/>");
				emptyElement = false;
				indentLevel--;
			}
			this.startElementOpened = false;
			indentLevel++;
		}
	}


	/**
	 * Writes a closing tag.
	 * @throws IOException
	 */
	private void writeCloseElement() throws IOException
	{
		indentLevel--;
		String qname = (String) this.qNameStack.pop();
		writeNewLine();
		write("</");
		write(qname);
		write(">");
		preventWhitespace = false;
	}


	/**
	 * Writes a newline and indent for the next line.
	 * @throws IOException
	 */
	private void writeNewLine() throws IOException
	{
		if (!preventNextLF  &&  !preventWhitespace)
		{
			write(newLineStr);
		}

		if (!preventWhitespace)
		{
			for (int i = baseIndent + indentLevel; i > 0; i--)
			{
				writer.write(indentStr);
			}
		}

		preventNextLF = false;
	}


	/**
	 * Asks if the namespace needs to be written or if it has already been defined.
	 * @param prefix a prefix
	 * @return Returns true if the namespace needs to be written.
	 */
	private boolean needToWriteNamespace(String prefix)
	{
		if (!registeredPrefixes.contains(prefix))
		{
			registeredPrefixes.add(prefix);
			return true;
		}
		else
		{
			return false;
		}
	}
}