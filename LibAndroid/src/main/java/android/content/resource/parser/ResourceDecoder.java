package android.content.resource.parser;

import android.content.pm.APKInfo;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.HashSet;


public class ResourceDecoder {
	
	public InputStream is = null;
	
	public short chunkType = -1;
	public int chunkSize = 0;
	
	public static final short TYPE_NONE = -1;
	public static final short TYPE_TABLE = 2;
	public static final short TYPE_PACKAGE = 512;
	public static final short TYPE_TYPE = 514;
	public static final short TYPE_CONFIG = 513;
	
	public int memberCount = 0;
	
	public  boolean mKeepBroken = false;
	
	public StringBlock sb = new StringBlock();
	
	public ResourcePackage[] blocks = null;
	
	public static int counter = -1 ;
	
	public static final CharsetDecoder UTF16LE_DECODER = Charset.forName("UTF-16LE").newDecoder();
	public static final CharsetDecoder UTF8_DECODER = Charset.forName("UTF-8").newDecoder();
	
	public ReturnValue rt = new ReturnValue();
	
	/*
	static
	{
		PrintStream ps= null;
		try {
			ps = new PrintStream(new FileOutputStream("e:/setting.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			if (Global.ASSISTANT_DEBUG)e.printStackTrace();
		}
		System.setOut(ps);
	}
	*/
	
	public boolean bPrint = false;
	
	public void addQury(int input)
	{
		rt.addQuery(input);
	}
	
	public void setQuery()
	{
		rt.setQuery();
	}
	
	public ResourceDecoder(InputStream is)
	{
		this.is = is;
	}
	
	public void Decoder(APKInfo info ,boolean keepBroken) throws Exception
	{
		this.mKeepBroken = keepBroken;
		
		nextChunk();
		if(chunkType != 2)
		{
			throw new Exception("YYY !="+2);
		}
		
		memberCount = ResourceReader.readInt(is);
		
		//System.out.println(memberCount);
		
		readBlock(sb);	
		
		nextChunk();
		
		blocks = new ResourcePackage[memberCount];
		for(int i = 0 ;i <memberCount ;i ++)
		{
			blocks[i] = new ResourcePackage();
			readPackage(blocks[i],info);			
		}
	}
	
	public void Decoder(boolean keepBroken) throws Exception
	{
		this.mKeepBroken = keepBroken;
		
		nextChunk();
		if(chunkType != 2)
		{
			throw new Exception("YYY !="+2);
		}
		
		memberCount = ResourceReader.readInt(is);
		
		//System.out.println(memberCount);
		
		readBlock(sb);
		
		/*
		for(int i = 0 ;i <sb.m_stringOffsets.length;i++)
		{
			System.out.println(getString(i,sb));
		}
		*/
		
		nextChunk();
		
		blocks = new ResourcePackage[memberCount];
		for(int i = 0 ;i <memberCount ;i ++)
		{
			blocks[i] = new ResourcePackage();
			readPackage(blocks[i]);			
			
		}
	}
	
	
	public  int nextChunk() throws Exception
	{
		if(is.available() < 2)
		{
			chunkType = -1;
			return -1;
		}
		
		chunkType = ResourceReader.readShort(is);
		ResourceReader.skipBytes(is, 2);
		chunkSize = ResourceReader.readInt(is);

		
		return 0;
	}
	
	public void readBlock(StringBlock sb) throws Exception
	{
		int chunk = ResourceReader.readInt(is);
		if(chunk != 1835009)
		{
			throw new Exception("ZZZ"+1835009);
		}
		
		sb._chunkSize = ResourceReader.readInt(is);
		sb._stringCount = ResourceReader.readInt(is);
		sb._styleOffsetCount = ResourceReader.readInt(is);
		int m = ResourceReader.readInt(is);
		sb._stringsOffset = ResourceReader.readInt(is);
		sb._stylesOffset = ResourceReader.readInt(is);
		
	
		
		
		sb.m_isUTF8 = ((m & 0x100) != 0);
		sb.m_stringOffsets =  ResourceReader.readIntArray(is, sb._stringCount);
		if (sb._styleOffsetCount != 0) 
		      sb.m_styleOffsets = ResourceReader.readIntArray(is, sb._styleOffsetCount);
		int i2 = (sb._stylesOffset == 0 ? sb._chunkSize : sb._stylesOffset) - sb._stringsOffset;
		if (i2 % 4 != 0)
		      throw new IOException(new StringBuilder().append("String data size is not multiple of 4 (").append(i2).append(").").toString());
		sb.m_strings = new byte[i2];
		ResourceReader.readFully(is, sb.m_strings);
		if (sb._stylesOffset != 0)
		{
			i2 = sb._chunkSize - sb._stylesOffset;
			if (i2 % 4 != 0)
				throw new IOException(new StringBuilder().append("Style data size is not multiple of 4 (").append(i2).append(").").toString());
			sb.m_styles = ResourceReader.readIntArray(is,i2 / 4);
		}		
	}
	
	public void readType(ResourcePackage sb,APKInfo info) throws Exception
	{
		int i = ResourceReader.readByte(is);
		ResourceReader.skipBytes(is, 3);
		int j = ResourceReader.readInt(is);
		
		sb.mMissingResSpecs = new boolean[j];
		Arrays.fill(sb.mMissingResSpecs, true);
		
		ResourceReader.skipBytes(is, j*4);
		sb.mResId = (0xFF000000 & sb.mResId | i << 16);
		//
		
		//System.out.println(String.format("0x%x", sb.mResId)+" == "+getString(i-1,sb.mTypeNames));
		nextChunk();
		
		while(this.chunkType==513)
		{			
			readConfig(sb);
			//TODO : set lang-country
			if( sb.mConfig!=null )
			{
				if( sb.mConfig.country == null && sb.mConfig.language == null)
				{
					continue;
				}
				if( info.country_lang_set ==null)
				{
					info.country_lang_set = new HashSet<String>();
				}
				
				StringBuilder _sb = new StringBuilder();
				if( sb.mConfig.language != null)
				{
					_sb.append(sb.mConfig.language);
				}
				else
					_sb.append(' ');
				
				_sb.append('@');
				
				if( sb.mConfig.country !=null)
				{
					_sb.append(sb.mConfig.country);
				}
				else
					_sb.append(' ');								
				info.country_lang_set.add(  _sb.toString() );			
			}
			
			int chunk =nextChunk();
			if(chunk <0)
			{
				this.chunkType = -1;
				return ;
			}
		}
		
	}
	
	public void readType(ResourcePackage sb) throws Exception
	{
		int i = ResourceReader.readByte(is);
		ResourceReader.skipBytes(is, 3);
		int j = ResourceReader.readInt(is);
		
		sb.mMissingResSpecs = new boolean[j];
		Arrays.fill(sb.mMissingResSpecs, true);
		
		ResourceReader.skipBytes(is, j*4);
		sb.mResId = (0xFF000000 & sb.mResId | i << 16);
		//
		
		//System.out.println(String.format("0x%x", sb.mResId)+" == "+getString(i-1,sb.mTypeNames));
		nextChunk();
		
		while(this.chunkType==513)
		{			
			readConfig(sb);
			int chunk =nextChunk();
			if(chunk <0)
			{
				this.chunkType = -1;
				return ;
			}
		}
		
	}
	
	public void readPackage(ResourcePackage sb ,APKInfo info) throws Exception
	{
		if(chunkType != 512 )
		{
			throw new Exception("xxxx"+512);
		}
		
		int i = ResourceReader.readInt(is);
		String str = ResourceReader.readNulEndString(is, 128, true);
		
		ResourceReader.skipBytes(is, 4);
		ResourceReader.skipBytes(is, 4);
		ResourceReader.skipBytes(is, 4);
		ResourceReader.skipBytes(is, 4);
		
		readBlock(sb.mTypeNames);
		readBlock(sb.mSpecNames);
		
		/*
		for(int l = 0 ;l <sb.mTypeNames.m_stringOffsets.length ;l ++)
		{
			System.out.println(getString(l,sb.mTypeNames));
		}
		*/
		
		sb.mResId = (i<<24);
		sb.mName = str;
		System.out.println("package:"+sb.mName+" --"+sb.mResId);
		
		nextChunk();
		while (this.chunkType == 514)
		{		
			readType(sb,info);
			
		}
	}
	
	public void readPackage(ResourcePackage sb) throws Exception
	{
		if(chunkType != 512 )
		{
			throw new Exception("xxxx"+512);
		}
		
		int i = ResourceReader.readInt(is);
		String str = ResourceReader.readNulEndString(is, 128, true);
		
		ResourceReader.skipBytes(is, 4);
		ResourceReader.skipBytes(is, 4);
		ResourceReader.skipBytes(is, 4);
		ResourceReader.skipBytes(is, 4);
		
		readBlock(sb.mTypeNames);
		readBlock(sb.mSpecNames);
		
		/*
		for(int l = 0 ;l <sb.mTypeNames.m_stringOffsets.length ;l ++)
		{
			System.out.println(getString(l,sb.mTypeNames));
		}
		*/
		
		sb.mResId = (i<<24);
		sb.mName = str;
		//System.out.println("package:"+sb.mName+" --"+sb.mResId);
		
		nextChunk();
		while (this.chunkType == 514)
		      readType(sb);
		
		
		/*
		int i = (byte)this.mIn.readInt();
		String str = this.mIn.readNulEndedString(128, true);
		this.mIn.skipInt();
		    this.mIn.skipInt();
		    this.mIn.skipInt();
		    this.mIn.skipInt();
		    this.mTypeNames = StringBlock.read(this.mIn);
		    this.mSpecNames = StringBlock.read(this.mIn);
		    this.mResId = (i << 24);
		    this.mPkg = new ResPackage(this.mResTable, i, str);
		    nextChunk();
		    while (this.mHeader.type == 514)
		      readType();
		      */
	}
	
	public void readConfig(ResourcePackage sp) throws Exception
	{
		ResourceReader.skipBytes(is, 4);
		//int s = ResourceReader.readInt(is);
		int i = ResourceReader.readInt(is);
		ResourceReader.skipBytes(is, 4);
		
	
		sp.mConfig = readConfigFlags();
		
		
		int[] arrayOfInt = ResourceReader.readIntArray(is,i);
		if( sp.mConfig != null)
		{
			if (sp.mConfig.isInvalid)
			{
				String str = sp.mName + sp.mConfig.mQualifiers;
				if (mKeepBroken)
				{
					//System.out.println("Invalid config flags detected: " + str);
				}
				else
				{
					//System.out.println("Invalid config flags detected. Dropping resources: " + str);
				}
			}
				
					
			sp.mConfig = ((sp.mConfig.isInvalid) && (!this.mKeepBroken) ? null : sp.getOrCreateConfig(sp.mConfig));
			
			//System.out.println("language:"+new String(sp.mConfig.language));
			//System.out.println("country:"+new String(sp.mConfig.country));
			if(sp.mConfig != null){
			    if(new String(sp.mConfig.language).equals("ZH") && new String(sp.mConfig.country).equals("CN"))
	            {
	                bPrint = true;
	            }
	            else
	                bPrint = false;
			}
			
		}		
		
		for (int j = 0; j < arrayOfInt.length; j++)
		{
			if (arrayOfInt[j] == -1)
				continue;
			sp.mMissingResSpecs[j] = false;
			int id = (sp.mResId & 0xFFFF0000 | j);
					
			
			//System.out.println(counter+" "+id+" ==  " + getString(counter++, sb));
			if(sp.mConfig != null){
			    readEntry(sp, id);
			}
		}
	}
	
	public void readEntry(ResourcePackage sp,int id) throws Exception
	{
		
		ResourceReader.skipBytes(is, 2);
		//short ss = ResourceReader.readShort(is);
		int i = ResourceReader.readShort(is);
		int j = ResourceReader.readInt(is);
	
		if((i & 0x1) == 0)
		{
			counter++;
			
			//System.out.println(counter+" "+id+" ==  " + getString(j, sp.mSpecNames));			
			String value = readValue(sp);
			if(rt.result != null )
			{
				int index = -1;
				for( int k = 0 ; k < rt.result.length ;k++)
				{
					if(rt.result[k].key == id)
					{
						index = k ;
						break;
					}
				}
				if(index>=0 && new String(sp.mConfig.language).equals("ZH") && new String(sp.mConfig.country).equals("CN"))
				{
					rt.result[index].value =  value;
				}
				else if( index>=0 && rt.result[index].value==null)
				{
					rt.result[index].value = value;
				}
			}
			
			
			
		}
		else
		{
			counter++;
			
			//System.out.println(counter+" "+id+" ==  " + getString(j, sp.mSpecNames));	
			String[] strs = readComplexEntry(sp);
			if(rt.result != null )
			{
				int index = -1;
				for( int k = 0 ; k < rt.result.length ;k++)
				{
					if(rt.result[k].key == id)
					{
						index = k ;						
						break;
					}
				}
				if(index>=0 && sp.mConfig != null && new String(sp.mConfig.language).equals("ZH") && new String(sp.mConfig.country).equals("CN"))
				{
					rt.result[index].value = strs[0];
				}
				else if( index>=0 && rt.result[index].value==null)
				{
					rt.result[index].value = strs[0];
				}
			}
			
			
			
			
			//counter++;
			//System.out.println(j+" "+id+" ==  " + getString(j, sp.mSpecNames));
			//counter--;
		}
		
		/*
		ResValue localResBagValue = (ResValue) ((i & 0x1) == 0 ? readValue() : readComplexEntry());
		if (this.mConfig == null)
			return;
		ResID localResID = new ResID(this.mResId);
		ResResSpec localResResSpec;
		if (this.mPkg.hasResSpec(localResID))
		{
			localResResSpec = this.mPkg.getResSpec(localResID);
		}
		else
		{
			localResResSpec = new ResResSpec(localResID, this.mSpecNames.getString(j), this.mPkg, this.mType);
			this.mPkg.addResSpec(localResResSpec);
			this.mType.addResSpec(localResResSpec);
		}
		ResResource localResResource = new ResResource(this.mConfig, localResResSpec, localResBagValue);
		this.mConfig.addResource(localResResource);
		localResResSpec.addResource(localResResource);
		this.mPkg.addResource(localResResource);
		*/
	}
	
	  public ResourceConfig readConfigFlags() throws Exception
	  {
		int i = ResourceReader.readInt(is);
		if(i <28)
		{
			throw new Exception("nweeeexx");
		}
	    boolean bool = false;
	    short s1 = ResourceReader.readShort(is);
	    short s2 = ResourceReader.readShort(is);
	    char[] arrayOfChar1 = { (char) ResourceReader.readByte(is), (char) ResourceReader.readByte(is) };
	    char[] arrayOfChar2 = { (char) ResourceReader.readByte(is), (char) ResourceReader.readByte(is) };
	    byte b1 = ResourceReader.readByte(is);
	    byte b2 = ResourceReader.readByte(is);
	    short s3 = ResourceReader.readShort(is);
	    byte b3 = ResourceReader.readByte(is);
	    byte b4 = ResourceReader.readByte(is);
	    byte b5 = ResourceReader.readByte(is);
	    ResourceReader.skipBytes(is,1);
	    short s4 = ResourceReader.readShort(is);
	    short s5 = ResourceReader.readShort(is);
	    short s6 = ResourceReader.readShort(is);
	    ResourceReader.skipBytes(is,2);
	    byte b6 = 0;
	    byte b7 = 0;
	    if (i >= 32)
	    {
	      b6 = ResourceReader.readByte(is);
	      b7 = ResourceReader.readByte(is);
	      ResourceReader.skipBytes(is,2);
	    }
	    int j = i - 32;
	    if (j > 0)
	    {
	      byte[] arrayOfByte = new byte[j];
	      ResourceReader.readFully(is,arrayOfByte);
	      BigInteger localBigInteger = new BigInteger(arrayOfByte);
	      if (localBigInteger.equals(BigInteger.ZERO))
	      {
	        //System.out.println(String.format("Config flags size > %d, but exceeding bytes are all zero, so it should be ok.", new Object[] { Integer.valueOf(32) }));
	      }
	      else
	      {
	        //System.out.println(String.format("Config flags size > %d. Exceeding bytes: %0" + j * 2 + "X.", new Object[] { Integer.valueOf(32), localBigInteger }));
	        bool = true;
	      }
	    }
	    return new ResourceConfig(s1, s2, arrayOfChar1, arrayOfChar2, b1, b2, s3, b3, b4, b5, s4, s5, s6, b6, b7, bool);
	  }
	
	  public String readValue(ResourcePackage sp) throws Exception
	  {
		   ResourceReader.readShort(is);
		   ResourceReader.readByte(is);
		   int i = ResourceReader.readByte(is);
		   int j = ResourceReader.readInt(is);
		   
		   if(i==3)
		   {
			  // System.out.println(getString(j,sb));
			   return getString(j,sb);
		   }
		   else
		   {
			  // System.out.println( " 3");
			   return null;
		   }
		   
		   //i == 3 ? this.mPkg.getValueFactory().factory(this.mTableStrings.getHTML(j)) : this.mPkg.getValueFactory().factory(i, j);
	  }
	  
	  public String[] readComplexEntry(ResourcePackage sp) throws Exception
	  {
		  int i = ResourceReader.readInt(is);
		  int j = ResourceReader.readInt(is);
		  //ResValueFactory localResValueFactory = this.mPkg.getValueFactory();
		  //Duo[] arrayOfDuo = new Duo[j];
		  String[] strs = new String[j];
		  for (int k = 0; k < j; k++)
		  {
			  ResourceReader.readInt(is);
			  String str = readValue(sp);
			  strs[k] = str;
			  //arrayOfDuo[k] = new Duo(Integer.valueOf(ResourceReader.readInt(is)), (ResScalarValue)readValue());
			  //System.out.println(""+ResourceReader.readInt(is)+"  "+readValue);
		  }
		  return strs;
		     //arrayOfDuo[k] = new Duo(Integer.valueOf(this.mIn.readInt()), (ResScalarValue)readValue());
		  //return localResValueFactory.bagFactory(i, arrayOfDuo);
	  }
	  
	
	  public String getHTML(int paramInt,StringBlock sb)
	  {
	    String str = getString(paramInt,sb);
	    if (str == null)
	      return str;
	    int[] arrayOfInt1 = getStyle(paramInt,sb);
	    if (arrayOfInt1 == null)
	      return escapeTextForResXml(str);
	    StringBuilder localStringBuilder = new StringBuilder(str.length() + 32);
	    int[] arrayOfInt2 = new int[arrayOfInt1.length / 3];
	    int i = 0;
	    int j = 0;
	    while (true)
	    {
	      int k = -1;
	      for (int m = 0; m != arrayOfInt1.length; m += 3)
	      {
	        if ((arrayOfInt1[(m + 1)] == -1) || ((k != -1) && (arrayOfInt1[(k + 1)] <= arrayOfInt1[(m + 1)])))
	          continue;
	        k = m;
	      }
	      int n = k != -1 ? arrayOfInt1[(k + 1)] : str.length();
	      int m = 0;
	      for (m = j - 1; m >= 0; m--)
	      {
	        int i1 = arrayOfInt2[m];
	        int i2 = arrayOfInt1[(i1 + 2)];
	        if (i2 >= n)
	          break;
	        if (i <= i2)
	        {
	          localStringBuilder.append(escapeCharsForResXml(str.substring(i, i2 + 1)));
	          i = i2 + 1;
	        }
	        outputStyleTag(getString(arrayOfInt1[i1],sb), localStringBuilder, true);
	      }
	      j = m + 1;
	      if (i < n)
	      {
	        localStringBuilder.append(escapeCharsForResXml(str.substring(i, n)));
	        i = n;
	      }
	      if (k == -1)
	        break;
	      String paramString = getString(arrayOfInt1[k],sb);
	      if(paramString != null) {
	    	  outputStyleTag(paramString, localStringBuilder, false);
	      }
	      arrayOfInt1[(k + 1)] = -1;
	      arrayOfInt2[(j++)] = k;
	    }
	    return escapeTextForResXml(localStringBuilder.toString(), false);
	  }
	  
	public static  String getString(int paramInt,StringBlock sb)
	{
		if ((paramInt < 0) || (sb == null) || (paramInt >= sb.m_stringOffsets.length))
			return null;
		int i = sb.m_stringOffsets[paramInt];
		int j= 0;
		if (!sb.m_isUTF8)
		{
			j = getShort(sb.m_strings, i) * 2;
			i += 2;
		}
		else
		{
			i += getVarint(sb.m_strings, i)[1];
			int[] arrayOfInt = getVarint(sb.m_strings, i);
			i += arrayOfInt[1];
			j = arrayOfInt[0];
		}
		return decodeString(sb,i, j);
	}
	
	 public static String escapeTextForResXml(String paramString)
	  {
	    return escapeTextForResXml(paramString, true);
	  }
	
	 public static String escapeTextForResXml(String paramString, boolean paramBoolean)
	  {
	    if (TextUtils.isEmpty(paramString))
	      return paramString;
	    if (paramBoolean)
	      paramString = escapeCharsForResXml(paramString);
	    StringBuilder localStringBuilder = new StringBuilder(paramString.length() + 10);
	    char[] arrayOfChar1 = paramString.toCharArray();
	    switch (arrayOfChar1[0])
	    {
	    case '#':
	    case '?':
	    case '@':
	      localStringBuilder.append('\\');
	    }
	    int i = 1;
	    for (char c : arrayOfChar1)
	      if (c == ' ')
	      {
	        if (i != 0)
	        {
	          localStringBuilder.append("\\u0020");
	        }
	        else
	        {
	          localStringBuilder.append(c);
	          i = 1;
	        }
	      }
	      else
	      {
	        i = 0;
	        localStringBuilder.append(c);
	      }
	    if ((i != 0) && (localStringBuilder.charAt(localStringBuilder.length() - 1) == ' '))
	    {
	      localStringBuilder.deleteCharAt(localStringBuilder.length() - 1);
	      localStringBuilder.append("\\u0020");
	    }
	    return localStringBuilder.toString();
	  }
	 
	  public static String escapeCharsForResXml(String paramString)
	  {
	    if (TextUtils.isEmpty(paramString))
	      return paramString;
	    StringBuilder localStringBuilder = new StringBuilder(paramString.length() + 10);
	    for (char c : paramString.toCharArray())
	    {
	      switch (c)
	      {
	      case '"':
	      case '\'':
	      case '\\':
	        localStringBuilder.append('\\');
	        break;
	      case '\n':
	        localStringBuilder.append("\\n");
	        break;
	      case '&':
	        localStringBuilder.append("&amp;");
	        break;
	      case '<':
	        localStringBuilder.append("&lt;");
	        break;
	      }
	      localStringBuilder.append(c);
	    }
	    return localStringBuilder.toString();
	  }
	
	
	  public int[] getStyle(int paramInt,StringBlock sb)
	  {
	    if ((sb.m_styleOffsets == null) || (sb.m_styles == null) || (paramInt >= sb.m_styleOffsets.length))
	      return null;
	    int i = sb.m_styleOffsets[paramInt] / 4;
	    int j = 0;
	    for (int k = i; (k < sb.m_styles.length) && (sb.m_styles[k] != -1); k++)
	      j++;
	    if ((j == 0) || (j % 3 != 0))
	      return null;
	    int[] arrayOfInt = new int[j];
	    j = i;
	   int k = 0;
	    while ((j < sb.m_styles.length) && (sb.m_styles[j] != -1))
	      arrayOfInt[(k++)] = sb.m_styles[(j++)];
	    return arrayOfInt;
	  }
	
	public static  int getShort(byte[] paramArrayOfByte, int paramInt)
	{
		return (paramArrayOfByte[(paramInt + 1)] & 0xFF) << 8 | paramArrayOfByte[paramInt] & 0xFF;
	}
	
	public static final int[] getVarint(byte[] paramArrayOfByte, int paramInt)
	{
		int i = paramArrayOfByte[paramInt];
		int j = (i & 0x80) != 0 ? 1 : 0;
		i &= 127;
		if (j == 0)
			return new int[] { i, 1 };
		return new int[] { i << 8 | paramArrayOfByte[(paramInt + 1)] & 0xFF, 2 };
	}
	
	public static String decodeString(StringBlock sb, int paramInt1, int paramInt2)
	{
		try
		{			
			return (sb.m_isUTF8 ? UTF8_DECODER : UTF16LE_DECODER).decode(ByteBuffer.wrap(sb.m_strings, paramInt1, paramInt2)).toString();
		}
		catch (CharacterCodingException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	
	 public void outputStyleTag(String paramString, StringBuilder paramStringBuilder, boolean paramBoolean)
	  {
	    paramStringBuilder.append('<');
	    if (paramBoolean)
	      paramStringBuilder.append('/');
	    
	    if (paramString != null) {//coverity id=61510
		    int i = paramString.indexOf(';');
		    if (i == -1)
		    {
		      paramStringBuilder.append(paramString);
		    }
		    else
		    {
		      paramStringBuilder.append(paramString.substring(0, i));
		      if (!paramBoolean)
		      {
		        int j = 1;
		        while (j != 0)
		        {
		          int k = paramString.indexOf('=', i + 1);
		          paramStringBuilder.append(' ').append(paramString.substring(i + 1, k)).append("=\"");
		          i = paramString.indexOf(';', k + 1);
		          String str;
		          if (i != -1)
		          {
		            str = paramString.substring(k + 1, i);
		          }
		          else
		          {
		            j = 0;
		            str = paramString.substring(k + 1);
		          }
		          paramStringBuilder.append(escapeCharsForResXml(str)).append('"');
		        }
		      }
		    }
		}
	    paramStringBuilder.append('>');	  
	  }
	 


}
