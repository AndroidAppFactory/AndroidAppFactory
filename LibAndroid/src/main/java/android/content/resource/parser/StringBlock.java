package android.content.resource.parser;

public class StringBlock {
	
	public boolean m_isUTF8 = true;
	
	public int _chunkSize = 0;
	public int _stringCount = 0;
	public int _styleOffsetCount = 0;
	public int _stringsOffset= 0;
	public int _stylesOffset = 0;
	
	public int[] m_stringOffsets = null;
	public int[] m_styleOffsets = null;
	public byte[] m_strings = null;
	public int[] m_styles = null;
}
