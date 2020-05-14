
/*
*@author ballandmo,2011-5-5
*all right reserved @2011
*/
package android.content.pm;

import android.content.resource.parser.ResourceTool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class APKParser 
{
	public static final int  FLAG_DECODE_RESOURCE =  1;
	
	public static final int  FLAG_GET_META_DATA  =  2;
	
	
	public static void main(String[] args)
	{
		File file = new File("E://orkspace_java/mms.apk");
		
		APKInfo info = parser(file,FLAG_DECODE_RESOURCE);
		getIcon(file,info);			
		System.out.println(info.iconPath+" >>"+ info.iconName+" :"+info.icon.length);
	}
	
	public static void getIcon(File apkFile ,APKInfo info)
	{
		if( info==null)
		{
			return ;
		}
		
		if( apkFile== null || !apkFile.exists())
		{
			return ;
		}
		
		if( info.iconPath == null )
		{
			return ;
		}
		
		ZipInputStream zis = null ;
		try {
			zis = new ZipInputStream( new FileInputStream(apkFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if( zis == null)
		{
			return ;
		}
		
		
		ByteArrayOutputStream baos =  null;
		ZipEntry zipEntry = null;	
		try {
			while( (zipEntry= zis.getNextEntry())!=null)
			{
				String _name = zipEntry.getName();
				//

				if (_name!=null &&_name.contains("../")) {
					continue;
				}
				//
				if( _name.equals(info.iconPath))
				{
					baos = new ByteArrayOutputStream();
					writeZipStreamTo(zis,baos);
					zis.closeEntry();		
				}				
				else
				{
					zis.closeEntry();
				}
			}
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		try {
			zis.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		if( baos == null)
		{
			return;
			
		}
		info.icon =  baos.toByteArray() ;
		try {
			baos.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return;
	}
	
	
	public static byte[] getIcon(File apkFile ,int flag,int id)
	{		
		APKInfo info = new APKInfo();
		info.iconResName  =  "@"+id;
		
		if( apkFile== null || !apkFile.exists())
		{
			return null;
		}
		
		ZipInputStream zis = null ;
		try {
			zis = new ZipInputStream( new FileInputStream(apkFile));
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
		if( zis == null)
		{
			return null;
		}		
		

		boolean decode = false;
		if( (flag & FLAG_DECODE_RESOURCE) > 0)
		{
			decode  = true;
		}
		
		ByteArrayOutputStream baos =  null;
		ZipEntry zipEntry = null;	
		try {
			while( (zipEntry= zis.getNextEntry())!=null)
			{
				String _name = zipEntry.getName();
				//

				if (_name!=null &&_name.contains("../")) {
					continue;
				}
				//
				if( _name.equals("resources.arsc") &&  decode)
				{
					baos = new ByteArrayOutputStream();
					writeZipStreamTo(zis,baos);
					baos.flush();
					zis.closeEntry();		
				}				
				else
				{
					try{
						zis.closeEntry();
					} catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}
		
		try {
			zis.close();
		} catch (Throwable e) {
			
			e.printStackTrace();
		}	
		
		boolean bool = ResourceTool.needResourceParser(info);
		if( !decode || !bool )
		{
			if( baos!=null)
			{
				try{
				baos.close();
				} catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			return null;
		}
		
		if( baos == null )
			return null;
		
		ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray());
		
		try {
			baos.close();
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		
		ResourceTool.resourceParser(info, bais);
		try {
			bais.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}	
		
		if( info.iconPath != null)
		{
			int index = info.iconPath.lastIndexOf('/');
			String name = info.iconPath.substring( index+ 1);
			
			info.iconName = name;
		}
		
		getIcon(apkFile,info);
		
		return info.icon;
	}
	
		
	public static APKInfo parser(File apkFile ,int flag)
	{
		APKInfo info = new APKInfo();
		
		if( apkFile== null || !apkFile.exists())
		{
			return info;
		}
		
		ZipInputStream zis = null ;
		try {
			zis = new ZipInputStream( new FileInputStream(apkFile));
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
		if( zis == null)
		{
			return info;
		}		
		

		boolean decode = false;
		if( (flag & FLAG_DECODE_RESOURCE) > 0)
		{
			decode  = true;
		}
		
		ByteArrayOutputStream baos =  null;
		ZipEntry zipEntry = null;	
		try {
			while( (zipEntry= zis.getNextEntry())!=null)
			{
				String _name = zipEntry.getName();
				//

				if (_name!=null &&_name.contains("../")) {
					continue;
				}
				//
				if( _name.equals("resources.arsc") &&  decode)
				{
					baos = new ByteArrayOutputStream();
					writeZipStreamTo(zis,baos);
					baos.flush();
					zis.closeEntry();		
				}
				else if( _name.equals("AndroidManifest.xml"))
				{
					AXMLPrinter.parserXML(zis, info,flag);	
					zis.closeEntry();
				}
				else
				{
					try{
						zis.closeEntry();
					} catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}
		
		try {
			zis.close();
		} catch (Throwable e) {
			
			e.printStackTrace();
		}	
		
		boolean bool = ResourceTool.needResourceParser(info);
		if( !decode || !bool )
		{
			if( baos!=null)
			{
				try{
				baos.close();
				} catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			return info;
		}
		
		if( baos == null )
			return info;
		
		ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray());
		
		try {
			baos.close();
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		
		ResourceTool.resourceParser(info, bais);
		try {
			bais.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}	
		
		if( info.iconPath != null)
		{
			int index = info.iconPath.lastIndexOf('/');
			String name = info.iconPath.substring( index+ 1);
			
			info.iconName = name;
		}
		
		return info;
	}
	
	public static void writeZipStreamTo(ZipInputStream zis ,OutputStream os)
	{
		byte[] buf = new byte[1024*8];
		
		int read = 1;
		try{
		while( read != -1 )
		{		
			read = zis.read(buf);
			if( read >0)
			os.write(buf,0,read);
		}
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
}


