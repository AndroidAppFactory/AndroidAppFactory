
/*
*@author ballandmo,2012-3-19
*all right reserved @2011
*/
package android.content.pm;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ReflectPackageParser 
{
	
	public static final int INSTALL_SUCCEEDED = 1;
	
	public static final int INSTALL_FAILED_INVALID_APK = -2;
	public static final int INSTALL_FAILED_INVALID_URI = -3;
	public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;
	public static final int INSTALL_FAILED_DUPLICATE_PACKAGE = -5;
	public static final int INSTALL_FAILED_NO_SHARED_USER = -6;
	public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7;
	public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8;
	public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9;
	public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10;
	public static final int INSTALL_FAILED_DEXOPT = -11;
	public static final int INSTALL_FAILED_OLDER_SDK = -12;
	public static final int INSTALL_FAILED_CONFLICTING_PROVIDER = -13;
	public static final int INSTALL_FAILED_NEWER_SDK = -14;
	public static final int INSTALL_FAILED_TEST_ONLY = -15;
	public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;
	public static final int INSTALL_FAILED_MISSING_FEATURE = -17;
	public static final int INSTALL_FAILED_CONTAINER_ERROR = -18;
	public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19;
	public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE = -20;
	public static final int INSTALL_FAILED_VERIFICATION_TIMEOUT = -21;
	public static final int INSTALL_FAILED_VERIFICATION_FAILURE = -22;
	public static final int INSTALL_FAILED_PACKAGE_CHANGED = -23;
	public static final int INSTALL_PARSE_FAILED_NOT_APK = -100;
	public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST = -101;
	public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION = -102;
	public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES = -103;
	public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;
	public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING = -105;
	public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME = -106;
	public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID = -107;
	public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED = -108;
	public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY = -109;
	public static final int INSTALL_FAILED_INTERNAL_ERROR = -110;
	
	public int err = INSTALL_SUCCEEDED ;
	
	//use this to 
	public Class cls = null;
	public Constructor con = null;	
	public Method p_method = null;
	
	public Resources res = null;
	public AssetManager assmgr  = null;
	
	@Override
	protected void finalize() throws Throwable 
	{
		// TODO Auto-generated method stub
		super.finalize();
		
		if(assmgr != null)
		{
			assmgr.close();
		}
	}

	public PackageInfo parserPcakge(String path , int flag) throws Exception
	{
		this.err = INSTALL_SUCCEEDED;
		//PackageParser parser = new PackageParser(path);
		
		if( cls == null)
		{
			 cls = Class.forName("android.content.pm.PackageParser");
		}
		if( con == null)
		{
			con = cls.getConstructor(String.class);
		}		
		if( p_method == null)
		{
			p_method  = cls.getDeclaredMethod("parsePackage", File.class,String.class,DisplayMetrics.class,int.class);
		}
		
		
		//packageparser object
		Object obj = con.newInstance(path);
		
		//Log.d("cn.imolo.call", obj.toString());
		
		Method me_get = cls.getMethod("getParseError");
		DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        
        //Log.d(Constant.LOG_TAG, "before invoke");
        Object obj_res =  null;
        try{
        	obj_res = p_method.invoke(obj, new File(path),path,metrics,0);
        } catch(Exception e)
        {
        	e.printStackTrace();
        }     
        //Log.d(Constant.LOG_TAG, "before get value");
        this.err = (Integer) me_get.invoke(obj);
        
        if( obj_res == null)
        {
        	return null;
        }
        
        //Log.d(Constant.LOG_TAG, "before getField");
        
        Class cl = obj_res.getClass();        
        Field field = cl.getDeclaredField("applicationInfo");        
        Field field_versionName = cl.getDeclaredField("mVersionName");
        Field field_versionCode = cl.getDeclaredField("mVersionCode");      
        
        ApplicationInfo ai = (ApplicationInfo)field.get(obj_res);
        String version = (String) field_versionName.get(obj_res);
        int version_code = field_versionCode.getInt(obj_res)  ;
                
       // Log.d("cn.imolo.call", info.packageName);
        
        //Log.d(Constant.LOG_TAG, "before getPermission");
        
        PackageInfo info = new PackageInfo();
        info.applicationInfo = ai;
        info.versionName =   version;        
        info.versionCode  = version_code;
        info.packageName = ai.packageName;
        
        //Log.d(Constant.LOG_TAG, "before getPermission");
        
        if( (flag &  PackageManager.GET_PERMISSIONS) > 0)
        {
        	Field field2 = cl.getDeclaredField("requestedPermissions");
        	Object o = field2.get(obj_res);
        	
        	//Log.d(Constant.LOG_TAG, "obj to arrayList");
        	ArrayList<String> requestedPermissions = (ArrayList<String>) o;
        	
        	//Log.d(Constant.LOG_TAG, " arrayList to String[]");
        	if( requestedPermissions != null )
        	{
        		int size = requestedPermissions.size();
        		if( size > 0)
        		info.requestedPermissions = new String[size];
        		for( int i = 0 ;i < size ;i++)
        		{
        			info.requestedPermissions[i] = requestedPermissions.get(i);
        		}
        	}        	   	
        }
        
        return info;
	}
	
	public String getString(Context  context ,String path ,int id) throws Exception
	{
		if( res == null)
		{
			getResource(context ,path );
		}
		if( res == null)
			return null;
		
		return res.getString(id);
	}
	
	public void close()
	{
		if( res != null)
		{
			res = null;
		}
		if( assmgr != null)
		{
			assmgr.close();
			assmgr = null;
		}
	}
	
	public Bitmap getDrawableBitmap(Context context ,String path , int id) throws Exception
	{
		if( res == null)
		{
			getResource(context ,path );
		}
		if( res == null)
			return null;
		
		Drawable icon = res.getDrawable(id);
		BitmapDrawable localBitmapDrawable = (BitmapDrawable) icon;
		Bitmap bitmap = localBitmapDrawable.getBitmap();	
		
		return bitmap;
	}
	
	public byte[] getDrwableBites(Context context,String path , int id) throws Exception 
	{
		Bitmap bitmap = getDrawableBitmap(context ,path ,id);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		
		byte[] bytes = baos.toByteArray();
		
		try{
		baos.close();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
		bitmap.recycle();
		
		return bytes;
	}
		
	public  ApplicationInfo parserPackage(String path   ) throws Exception 
	{
		this.err = INSTALL_SUCCEEDED;
		//PackageParser parser = new PackageParser(path);
		
		if( cls == null)
		{
			 cls = Class.forName("android.content.pm.PackageParser");
		}
		if( con == null)
		{
			con = cls.getConstructor(String.class);
		}		
		if( p_method == null)
		{
			p_method  = cls.getDeclaredMethod("parsePackage", File.class,String.class,DisplayMetrics.class,int.class);
		}
		
		
		//packageparser object
		Object obj = con.newInstance(path);
		
		//Log.d("cn.imolo.call", obj.toString());
		
		Method me_get = cls.getMethod("getParseError");
		DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        
        Object obj_res =  null;
        try{
        	obj_res = p_method.invoke(obj, new File(path),path,metrics,0);
        } catch(Exception e)
        {
        	e.printStackTrace();
        }        
        
        if( obj_res == null)
        {
        	this.err = (Integer) me_get.invoke(obj);
        	return null;
        }
        
        Class cl = obj_res.getClass();        
        Field field = cl.getDeclaredField("applicationInfo");
        
        ApplicationInfo info = (ApplicationInfo)field.get(obj_res);
                
       // Log.d("cn.imolo.call", info.packageName);]
              
        
        return info;
	}
	
	public void getResource(Context context ,String path) throws Exception
	{
		if( res != null)
			return;
		
		Resources pRes = context.getResources();
		if( assmgr == null)
		{
			assmgr = AssetManager.class.newInstance();

			Method method1 = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);

			method1.invoke(assmgr, path);
		}
		
		res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration()); 
	}
	
	public static Bitmap getResourceDrawable(Context context,String path , int id) throws Exception 
	{
		 
		if( id  ==0)
			return null;
		
		Resources pRes = context.getResources();
		AssetManager assmgr = AssetManager.class.newInstance();

		Method method1 = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);

		method1.invoke(assmgr, path);

		Resources res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration()); 
             
        Drawable icon = res.getDrawable(id); 
        
        BitmapDrawable localBitmapDrawable = (BitmapDrawable) icon;
		Bitmap bitmap = localBitmapDrawable.getBitmap();	
		
		
		assmgr.close();
        
        return bitmap;
	}
	
	public static Drawable getDrawable(Context context,String path , int id) throws Exception 
	{		 
		if( id  ==0)
			return null;
		
		Resources pRes = context.getResources();
		AssetManager assmgr = AssetManager.class.newInstance();

		Method method1 = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);

		method1.invoke(assmgr, path);

		Resources res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration()); 
             
        Drawable icon = res.getDrawable(id);        
        
        return icon;
	}
	
	public static byte[] getDrawableBytes(Context context,String path , int id) throws Exception 
	{
		Bitmap bitmap = getResourceDrawable(context ,path ,id);
		
		if(bitmap == null) {
			return null;
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		
		byte[] bytes = baos.toByteArray();
		
		try{
		baos.close();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
		bitmap.recycle();
		
		return bytes;
	}
	
	 

}


