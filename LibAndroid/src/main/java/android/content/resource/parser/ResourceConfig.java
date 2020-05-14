package android.content.resource.parser;

public class ResourceConfig {
	
	  public final short mcc;
	  public final short mnc;
	  public final char[] language;
	  public final char[] country;
	  public final byte orientation;
	  public final byte touchscreen;
	  public final short density;
	  public final byte keyboard;
	  public final byte navigation;
	  public final byte inputFlags;
	  public final short screenWidth;
	  public final short screenHeight;
	  public final short sdkVersion;
	  public final byte screenLayout;
	  public final byte uiMode;
	  public final boolean isInvalid;
	  public final String mQualifiers;
	  public static int sErrCounter = 0;
	  
	  public ResourceConfig()
	  {
	    this.mcc = 0;
	    this.mnc = 0;
	    this.language = new char[] { '\000', '\000' };
	    this.country = new char[] { '\000', '\000' };
	    
	   
	    this.orientation = 0;
	    this.touchscreen = 0;
	    this.density = 0;
	    this.keyboard = 0;
	    this.navigation = 0;
	    this.inputFlags = 0;
	    this.screenWidth = 0;
	    this.screenHeight = 0;
	    this.sdkVersion = 0;
	    this.screenLayout = 0;
	    this.uiMode = 0;
	    this.isInvalid = false;
	    this.mQualifiers = "";
	  }
	  
	  public ResourceConfig(short paramShort1, short paramShort2, char[] paramArrayOfChar1, char[] paramArrayOfChar2, byte paramByte1, byte paramByte2, short paramShort3, byte paramByte3, byte paramByte4, byte paramByte5, short paramShort4, short paramShort5, short paramShort6, byte paramByte6, byte paramByte7, boolean paramBoolean)
	  {
	    if ((paramByte1 < 0) || (paramByte1 > 3))
	    {
	      //System.out.println(new StringBuilder().append("Invalid orientation value: ").append(paramByte1).toString());
	      paramByte1 = 0;
	      paramBoolean = true;
	    }
	    if ((paramByte2 < 0) || (paramByte2 > 3))
	    {
	      //System.out.println(new StringBuilder().append("Invalid touchscreen value: ").append(paramByte2).toString());
	      paramByte2 = 0;
	      paramBoolean = true;
	    }
	    if (paramShort3 < -1)
	    {
	      //System.out.println(new StringBuilder().append("Invalid density value: ").append(paramShort3).toString());
	      paramShort3 = 0;
	      paramBoolean = true;
	    }
	    if ((paramByte3 < 0) || (paramByte3 > 3))
	    {
	      //System.out.println(new StringBuilder().append("Invalid keyboard value: ").append(paramByte3).toString());
	      paramByte3 = 0;
	      paramBoolean = true;
	    }
	    if ((paramByte4 < 0) || (paramByte4 > 4))
	    {
	      //System.out.println(new StringBuilder().append("Invalid navigation value: ").append(paramByte4).toString());
	      paramByte4 = 0;
	      paramBoolean = true;
	    }
	    this.mcc = paramShort1;
	    this.mnc = paramShort2;
	    this.language = paramArrayOfChar1;
	    this.country = paramArrayOfChar2;
	    this.orientation = paramByte1;
	    this.touchscreen = paramByte2;
	    this.density = paramShort3;
	    this.keyboard = paramByte3;
	    this.navigation = paramByte4;
	    this.inputFlags = paramByte5;
	    this.screenWidth = paramShort4;
	    this.screenHeight = paramShort5;
	    this.sdkVersion = paramShort6;
	    this.screenLayout = paramByte6;
	    this.uiMode = paramByte7;
	    this.isInvalid = paramBoolean;
	    this.mQualifiers = "";
	    //this.mQualifiers = generateQualifiers();
	    
	 
	  }
	  
	  public String generateQualifiers()
	  {
	    StringBuilder localStringBuilder = new StringBuilder();
	    if (this.mcc != 0)
	    {
	      localStringBuilder.append("-mcc").append(this.mcc);
	      if (this.mnc != 0)
	        localStringBuilder.append("-mnc").append(this.mnc);
	    }
	    if (this.language[0] != 0)
	    {
	      localStringBuilder.append('-').append(this.language);
	      if (this.country[0] != 0)
	        localStringBuilder.append("-r").append(this.country);
	    }
	    switch (this.screenLayout & 0xF)
	    {
	    case 1:
	      localStringBuilder.append("-small");
	      break;
	    case 2:
	      localStringBuilder.append("-normal");
	      break;
	    case 3:
	      localStringBuilder.append("-large");
	    }
	    switch (this.screenLayout & 0x30)
	    {
	    case 32:
	      localStringBuilder.append("-long");
	      break;
	    case 16:
	      localStringBuilder.append("-notlong");
	    }
	    switch (this.orientation)
	    {
	    case 1:
	      localStringBuilder.append("-port");
	      break;
	    case 2:
	      localStringBuilder.append("-land");
	      break;
	    case 3:
	      localStringBuilder.append("-square");
	    }
	    switch (this.uiMode & 0xF)
	    {
	    case 3:
	      localStringBuilder.append("-car");
	      break;
	    case 2:
	      localStringBuilder.append("-desk");
	    }
	    switch (this.uiMode & 0x30)
	    {
	    case 32:
	      localStringBuilder.append("-night");
	      break;
	    case 16:
	      localStringBuilder.append("-notnight");
	    }
	    switch (this.density)
	    {
	    case 0:
	      break;
	    case 120:
	      localStringBuilder.append("-ldpi");
	      break;
	    case 160:
	      localStringBuilder.append("-mdpi");
	      break;
	    case 240:
	      localStringBuilder.append("-hdpi");
	      break;
	    case -1:
	      localStringBuilder.append("-nodpi");
	      break;
	    default:
	      localStringBuilder.append('-').append(this.density).append("dpi");
	    }
	    switch (this.touchscreen)
	    {
	    case 1:
	      localStringBuilder.append("-notouch");
	      break;
	    case 2:
	      localStringBuilder.append("-stylus");
	      break;
	    case 3:
	      localStringBuilder.append("-finger");
	    }
	    switch (this.inputFlags & 0x3)
	    {
	    case 1:
	      localStringBuilder.append("-keysexposed");
	      break;
	    case 2:
	      localStringBuilder.append("-keyshidden");
	      break;
	    case 3:
	      localStringBuilder.append("-keyssoft");
	    }
	    switch (this.keyboard)
	    {
	    case 1:
	      localStringBuilder.append("-nokeys");
	      break;
	    case 2:
	      localStringBuilder.append("-qwerty");
	      break;
	    case 3:
	      localStringBuilder.append("-12key");
	    }
	    switch (this.inputFlags & 0xC)
	    {
	    case 4:
	      localStringBuilder.append("-navexposed");
	      break;
	    case 8:
	      localStringBuilder.append("-navhidden");
	    }
	    switch (this.navigation)
	    {
	    case 1:
	      localStringBuilder.append("-nonav");
	      break;
	    case 2:
	      localStringBuilder.append("-dpad");
	      break;
	    case 3:
	      localStringBuilder.append("-trackball");
	      break;
	    case 4:
	      localStringBuilder.append("-wheel");
	    }
	    if ((this.screenWidth != 0) && (this.screenHeight != 0))
	      if (this.screenWidth > this.screenHeight)
	        localStringBuilder.append(String.format("-%dx%d", new Object[] { Short.valueOf(this.screenWidth), Short.valueOf(this.screenHeight) }));
	      else
	        localStringBuilder.append(String.format("-%dx%d", new Object[] { Short.valueOf(this.screenHeight), Short.valueOf(this.screenWidth) }));
	    if (this.sdkVersion > getNaturalSdkVersionRequirement())
	      localStringBuilder.append("-v").append(this.sdkVersion);
	    if (this.isInvalid)
	      localStringBuilder.append(new StringBuilder().append("-ERR").append(sErrCounter++).toString());
	    return localStringBuilder.toString();
	  }
	  
	  public short getNaturalSdkVersionRequirement()
	  {
	    if ((this.uiMode & 0x3F) != 0)
	      return 8;
	    if (((this.screenLayout & 0x3F) != 0) || (this.density != 0))
	      return 4;
	    return 0;
	  }

}
