package com.bihe0832.android.framework.update;

import com.google.gson.annotations.SerializedName;

public class UpdateDataFromCloud {

    public static final int UPDATE_TYPE_NEW = 0;
    public static final int UPDATE_TYPE_HAS_NEW_JUMP = 1;
    public static final int UPDATE_TYPE_HAS_NEW = 2;
    public static final int UPDATE_TYPE_RED_JUMP = 3;
    public static final int UPDATE_TYPE_RED = 4;
    public static final int UPDATE_TYPE_NEED_JUMP = 5;
    public static final int UPDATE_TYPE_NEED = 6;
    public static final int UPDATE_TYPE_MUST_JUMP = 7;
    public static final int UPDATE_TYPE_MUST = 8;

    //更新类型
    private int updateType = UPDATE_TYPE_NEW;

    //红点更新最高版本号、可选项（低于该版本显示红点、可选、强更无需配置必有红点）
    @SerializedName("showRedMaxVersionCode")
    private int showRedMaxVersionCode = 0;

    //可选更新最高版本号、可选项（该版本及更低版本弹框）
    @SerializedName("needUpdateMinVersionCode")
    private int needUpdateMinVersionCode = 0;

    //强制更新最高版本号、可选项（该版本及更低版本强更）
    @SerializedName("forceUpdateMinVersionCode")
    private int forceUpdateMinVersionCode = 0;

    //可选更新特殊版本列表、可选项，该列表的版本号即使高于 needUpdateMinVersionCode 如果匹配也弹更新框
    @SerializedName("needUpdateList")
    private String needUpdateList = "";

    //强制更新特殊版本列表、可选项，该列表的版本号即使高于 forceUpdateMinVersionCode 如果匹配也弹强制更新框
    @SerializedName("forceUpdateList")
    private String forceUpdateList = "";

	//新版本版本号、必填项
	@SerializedName("newVersionCode")
	private int newVersionCode = 0;

    //新版本版本名、必填项
    @SerializedName("newVersionName")
    private String newVersionName = "";

    //新版本版本介绍标题、非必填项
    @SerializedName("newVersionTitle")
    private String newVersionTitle = "";

    //新版本版本介绍、必填项
    @SerializedName("newVersionInfo")
    private String newVersionInfo = "";

    //新版本APK文件MD5，可选项
    @SerializedName("newVersionMD5")
    private String newVersionMD5 = "";

    //新版本APK下载地址或者跳转URL、必填项，如果为URL，跳转到新页面，如果是APK，则下载
    @SerializedName("newVersionURL")
    private String newVersionURL = "";

    public void setNeedUpdateMinVersionCode(int needUpdateMinVersionCode) {
        this.needUpdateMinVersionCode = needUpdateMinVersionCode;
    }

	public int getShowRedMaxVersionCode() {
		return showRedMaxVersionCode;
	}

	public int getNewVersionCode() {
		return newVersionCode;
	}

	public void setNewVersionCode(int newVersionCode) {
		this.newVersionCode = newVersionCode;
	}

	public void setShowRedMaxVersionCode(int showRedMaxVersionCode) {
		this.showRedMaxVersionCode = showRedMaxVersionCode;
	}

	public int getNeedUpdateMinVersionCode() {
        return needUpdateMinVersionCode;
    }

    public void setNewVersionName(String newVersionName) {
        this.newVersionName = newVersionName;
    }

    public String getNewVersionName() {
        return newVersionName;
    }

    public void setNeedUpdateList(String needUpdateList) {
        this.needUpdateList = needUpdateList;
    }

    public String getNeedUpdateList() {
        return needUpdateList;
    }

    public String getForceUpdateList() {
        return forceUpdateList;
    }

    public void setNewVersionMD5(String newVersionMD5) {
        this.newVersionMD5 = newVersionMD5;
    }

    public String getNewVersionMD5() {
        return newVersionMD5;
    }

    public void setNewVersionURL(String newVersionURL) {
        this.newVersionURL = newVersionURL;
    }

    public String getNewVersionURL() {
        return newVersionURL;
    }

    public void setForceUpdateMinVersionCode(int forceUpdateMinVersionCode) {
        this.forceUpdateMinVersionCode = forceUpdateMinVersionCode;
    }

    public int getForceUpdateMinVersionCode() {
        return forceUpdateMinVersionCode;
    }

    public void setNewVersionInfo(String newVersionInfo) {
        this.newVersionInfo = newVersionInfo;
    }

    public String getNewVersionInfo() {
        return newVersionInfo;
    }


    public String getNewVersionTitle() {
        return newVersionTitle;
    }

    public void setNewVersionTitle(String newVersionTitle) {
        this.newVersionTitle = newVersionTitle;
    }

    public int getUpdateType() {
        return updateType;
    }

    public void setUpdateType(int updateType) {
        this.updateType = updateType;
    }

    public boolean canShowNew(){
        return  updateType > UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW;
    }

    @Override
    public String toString() {
        return
                "Response{" +
                        " showRedMaxVersionCode = '" + showRedMaxVersionCode + '\'' +
                        "needUpdateMinVersionCode = '" + needUpdateMinVersionCode + '\'' +
                        ",newVersionName = '" + newVersionName + '\'' +
                        ",newVersionCode = '" + newVersionCode + '\'' +
                        ",needUpdateList = '" + needUpdateList + '\'' +
                        ",newVersionMD5 = '" + newVersionMD5 + '\'' +
                        ",newVersionURL = '" + newVersionURL + '\'' +
                        ",forceUpdateMinVersionCode = '" + forceUpdateMinVersionCode + '\'' +
                        ",newVersionInfo = '" + newVersionInfo + '\'' +
                        "}";
    }
}