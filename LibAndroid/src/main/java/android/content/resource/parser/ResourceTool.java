package android.content.resource.parser;

import android.content.pm.APKInfo;

import java.io.FileInputStream;
import java.io.InputStream;


public class ResourceTool {
	
	public static boolean needResourceParser(APKInfo info)
	{
		if( info == null)
		{
			return false;
		}
		
		if( info.application_res_name != null && info.application_res_name.startsWith("@"))
		{
			return true;
		}
		
		if( info.versionName != null && info.versionName.startsWith("@"))
		{
			return true;
		}
		
		if( info.iconResName !=null && info.iconResName.startsWith("@"))
		{
			return true;
		}
		return false;		
	}
	
	public static void resourceParser(APKInfo info ,InputStream is)
	{
		ResourceDecoder decoder = new ResourceDecoder(is);
		
		int name_index = 0;
		int icon_index = 0;
		int version_index = 0;
		
		try{
		if( info.application_res_name != null && info.application_res_name.startsWith("@"))
		{
			String subString = info.application_res_name.substring(1);
			name_index  = Integer.parseInt(subString, 10);
			decoder.addQury(name_index);
		}
		if( info.versionName != null && info.versionName.startsWith("@"))
		{

			String subString = info.versionName.substring(1);
			version_index  = Integer.parseInt(subString, 10);
			decoder.addQury(version_index);
		}
		
		if( info.iconResName != null && info.iconResName.startsWith("@"))
		{
			String subString = info.iconResName.substring(1);
			icon_index  = Integer.parseInt(subString, 10);
			decoder.addQury(icon_index);
		}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
		decoder.setQuery();
		try {
			decoder.Decoder(info,true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i = 0 ; i<decoder.rt.result.length ;i++)
		{
			//System.out.println(decoder.rt.result[i].key+" = "+decoder.rt.result[i].value);
			if( decoder.rt.result[i].key == name_index )
			{
				info.application_name = decoder.rt.result[i].value;
			}
			else if( decoder.rt.result[i].key == icon_index)
			{
				info.iconPath = decoder.rt.result[i].value;
			}
			else if( decoder.rt.result[i].key == version_index)
			{
				info.versionName = decoder.rt.result[i].value;
			}
		}
		
	}
	

	
	public static void main(String[] args)
	{
		//System.out.println("XXXXXXXXXXXXXXXXXxx");
		Decoder("e:/resources.arsc");
	}
	
	public static void Decoder(String filePath)
	{
		//System.out.println("HHHHHHHHHHHHHHHHHHH");
		try{
		InputStream is = new FileInputStream(filePath);		
		
		
		ResourceDecoder decoder = new ResourceDecoder(is);
		decoder.addQury(0x7F08002C);
		decoder.setQuery();
		decoder.Decoder(true);
		
		//System.out.println("result");
		for(int i = 0 ; i<decoder.rt.result.length ;i++)
		{
			System.out.println(decoder.rt.result[i].key+" = "+decoder.rt.result[i].value);
		}
		
		is.close();
		} catch(Throwable e)
		{
			e.printStackTrace();
		}
		
	}
}
