package com.bihe0832.android.common.message.data;

import com.bihe0832.android.lib.utils.time.DateUtil;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class MessageInfoItem {

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_IMG = "image";
    public static final String TYPE_WEB_PAGE = "web";
    public static final String TYPE_APK = "apk";
    // 公告ID，可以用于更新推送
    @SerializedName("id")
    private String messageID;
    //公告类型
    @SerializedName("type")
    private String type = MessageInfoItem.TYPE_TEXT;

    @SerializedName("position")
    private String position;
    // 公告标题
    @SerializedName("title")
    private String title;
    // 公告内容 如果是文本信息，则为文本信息，如果是图片则为图片的url（jpg），如果是H5页面则为该页面的url
    @SerializedName("content")
    private String content = "";
    // 公告点击动作
    @SerializedName("action")
    private String action = "";
    //发布时间
    @SerializedName("create_date")
    private String createDate = "";
    // 信息过期日期，过期的信息将会被自动删除，日期格式YYYYMMDDHHmm (201901010000 表示2019年1月1日0时0分)，如果不需要过期日期，则该字段为-1
    @SerializedName("expire_date")
    private String expireDate = "";
    // 是否置顶  0为false，1为true，（如果有不止一条置顶信息，则遵循后来者居上原则）
    @SerializedName("should_top")
    private String shouldTop = "0";
    // 是否同步通知栏
    @SerializedName("isNotify")
    private String isNotify = "0";
    // 同步通知栏描述
    @SerializedName("notifyDesc")
    private String notifyDesc = "";
    @SerializedName("notifyChannelName")
    private String notifyChannelName = "";
    @SerializedName("notifyChannelID")
    private String notifyChannelID = "";
    //是否可以删除
    @SerializedName("canDelete")
    private String canDelete = "1";

    //拍脸展示次数，当为-1 表示一直拍
    @SerializedName("showFace")
    private int showFace = 0;

    private transient boolean hasDelete = false;
    private transient boolean hasRead = false;
    private transient long lastShowTime = 0L;

    public boolean canShow(boolean isFace) {
        if (isFace) {
            if (showFace > 0) {
                return isNotExpired() && !hasDelete();
            } else {
                return false;
            }
        } else {
            return isNotExpired() && !hasDelete();
        }
    }

    public boolean isNotExpired() {
        return expireDate.equalsIgnoreCase("-1") || DateUtil.compareDate(new Date(), expireDate, "yyyyMMddHHmm") <= 0;
    }

    public String getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(String canDelete) {
        this.canDelete = canDelete;
    }

    public String getIsNotify() {
        return isNotify;
    }

    public void setIsNotify(String isNotify) {
        this.isNotify = isNotify;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setShouldTop(String shouldTop) {
        this.shouldTop = shouldTop;
    }

    public String getShouldTop() {
        return shouldTop;
    }

    public void setMessageID(String mID) {
        this.messageID = mID;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getNotifyDesc() {
        return notifyDesc;
    }

    public void setNotifyDesc(String notifyDesc) {
        this.notifyDesc = notifyDesc;
    }

    public int getShowFace() {
        return showFace;
    }

    public void setShowFace(int showFace) {
        this.showFace = showFace;
    }

    public boolean hasDelete() {
        return hasDelete;
    }

    public void setHasDelete(boolean hasDelete) {
        this.hasDelete = hasDelete;
    }

    public boolean hasRead() {
        return hasRead;
    }

    public void setHasRead(boolean hasRead) {
        this.hasRead = hasRead;
    }

    public String getNotifyChannelName() {
        return notifyChannelName;
    }

    public void setNotifyChannelName(String notifyChannelName) {
        this.notifyChannelName = notifyChannelName;
    }

    public String getNotifyChannelID() {
        return notifyChannelID;
    }

    public void setNotifyChannelID(String notifyChannelID) {
        this.notifyChannelID = notifyChannelID;
    }

    public long getLastShow() {
        return lastShowTime;
    }

    public void setLastShow(long lastShowTime) {
        this.lastShowTime = lastShowTime;
    }

    public void copyFrom(MessageInfoItem oldData) {
        this.messageID = oldData.messageID;
        this.type = oldData.type;
        this.title = oldData.title;
        this.content = oldData.content;
        this.action = oldData.action;
        this.createDate = oldData.createDate;
        this.isNotify = oldData.isNotify;
        this.shouldTop = oldData.shouldTop;
        this.notifyDesc = oldData.notifyDesc;
        this.notifyChannelID = oldData.notifyChannelID;
        this.notifyChannelName = oldData.notifyChannelName;
        this.canDelete = oldData.canDelete;
        this.showFace = oldData.showFace;

        this.hasDelete = oldData.hasDelete;
        this.hasRead = oldData.hasRead;
        this.lastShowTime = oldData.lastShowTime;

    }

    @Override
    public String toString() {
        return "MessageInfoItem{" + "id='" + messageID + '\'' + ", type=" + type + ", title='" + title + '\''  + ", action='" + action + '\'' + ", lastshow='" + lastShowTime + '\'' + ", expireDate='" + expireDate + '\'' + ", shouldTop='" + shouldTop + '\'' + ", isNotify='" + isNotify + '\''  + ", canDelete='" + canDelete + '\'' + ", showFace='" + showFace + '\'' + '}';
    }
}