
/*
*@author ballandmo,2011-5-5
*all right reserved @2011
*/
package android.content.pm;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashSet;

public class APKInfo 
{
	public static final String PACKAGE = "package";
	public static final String VERSION_NAME = "versionName";
	public static final String VERSION_CODE = "versionCode";
	public static final String MINI_SDK_VERSION = "minSdkVersion";
	
	public static final String APPLICATION = "application";
	public static final String ICON = "icon";
	public static final String NAME = "label";
	public static final String PERMISSION_TAG = "uses-permission";
	
	public static final String META_DATA = "meta-data";
	
	public static final String ANDROID_NAME = "name";
	public static final String ANDROID_VALUE = "value";
	
	public String packageName = null;
	public String versionName = null;
	public int versionCode =  -1 ;
	public int miniSDKVersion = -1;
	
	public ArrayList<String> permission_list = null;
	public String iconResName = null;
	
	//
	public String iconPath = null;
	public String iconName = null;
	public byte[] icon = null;
	
	public Bundle metaData = null;
	
	public String application_res_name = null;
	public String application_name = null;
	
	public HashSet<String>  country_lang_set =  null;	
}


