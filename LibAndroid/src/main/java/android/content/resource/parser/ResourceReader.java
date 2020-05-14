package android.content.resource.parser;

import java.io.InputStream;

public class ResourceReader {
	
	public static  short readShort(InputStream is) throws Exception
	{
		byte[] bytes   = new byte[2];
		int read  = is.read(bytes);
		if(read<2)
		{
			throw new Exception("XXXX"+2);
		}
		
		return (short)((bytes[1] << 8) | (bytes[0] & 0xff));
	}
	
	public static void skipBytes(InputStream is ,int count) throws Exception
	{
		is.skip(count);
	}
	
	public static int readInt(InputStream is ) throws Exception
	{
		byte[] bytes   = new byte[4];
		int read  = is.read(bytes);
		if(read<4)
		{
			throw new Exception("XXXX"+4);
		}
		
		return	(((bytes[3] & 0xff) << 24) | ((bytes[2] & 0xff) << 16) |
				  ((bytes[1] & 0xff) << 8) | (bytes[0] & 0xff));
				 
	}
	
	public static byte readByte(InputStream is) throws Exception
	{
		byte[] bytes = new byte[1];
		is.read(bytes);
		return bytes[0];
	}
	
	public static int[] readIntArray(InputStream is ,int size) throws Exception
	{
		int[] intArray = new int[size];
		for (int i = 0; i < size; i++)
			intArray[i] = readInt(is);
		return intArray;
	}
	
	public static void readFully(InputStream is ,byte[] bytes) throws Exception
	{
		is.read(bytes);
	}
	
	public static String readNulEndString(InputStream is,int length ,boolean bool) throws Exception
	{
		StringBuilder sb = new StringBuilder(16);
		while (length-- != 0)
		{
			int i = readShort(is);
			if (i == 0)
				break;
			sb.append((char)i);
		}
		if (bool)
			skipBytes(is,length * 2);
		return sb.toString();
	}
	
	
	  
	   
}
