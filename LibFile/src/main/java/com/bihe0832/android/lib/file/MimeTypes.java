/*
 * Copyright (C) 2016 Jecelyin Peng <jecelyin@gmail.com>
 *
 * This file is part of 920 Text Editor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bihe0832.android.lib.file;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class MimeTypes {

    private ConcurrentHashMap<String, String> mMimeTypes = new ConcurrentHashMap<>();

    private static MimeTypes instance = null;

    public static MimeTypes getInstance() {
        if (instance == null)
            instance = new MimeTypes();
        return instance;
    }


    public void put(String type, String extension) {
        // Convert extensions to lower case letters for easier comparison
        extension = extension.toLowerCase();
        mMimeTypes.put(type, extension);
    }

    public String getMimeType(String filename) {
        String extension = FileUtils.INSTANCE.getExtensionName(filename);
        if (TextUtils.isEmpty(extension)) {
            return "*/*";
        }
        String mimetype = mMimeTypes.get(extension.toLowerCase());
        if (mimetype == null) {
            String webkitMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
            if (webkitMimeType != null) {
                put(extension, webkitMimeType);
                return webkitMimeType;
            } else {
                return "*/*";
            }
        } else {
            return mimetype;
        }
    }


    /**
     * Indicates whether file is considered to be "text".
     *
     * @return {@code true} if file is text, {@code false} if not.
     */
    public boolean isTextFile(File file) {
        return !file.isDirectory() && isMimeText(getMimeType(file.getPath()));
    }

    public static boolean isMimeText(String mime) {
        return mime.startsWith("text");
    }

    /**
     * Indicates whether this path is an image.
     *
     * @return {@code true} if file is image, {@code false} if not.
     */
    public boolean isImageFile(File file) {
        return !file.isDirectory() && isImageFile(file.getName());
    }

    /**
     * Indicates whether requested file path is an image. This is done by
     * comparing file extension to a static list of extensions known to be
     * images.
     *
     * @param file File path
     * @return {@code true} if file is image, {@code false} if not.
     */
    public static boolean isImageFile(String file) {
        String ext = file.substring(file.lastIndexOf(".") + 1);
        if (MimeTypes.getInstance().getMimeType(file).startsWith("image/"))
            return true;
        return ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("jpg")
                || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("gif")
                || ext.equalsIgnoreCase("tiff") || ext.equalsIgnoreCase("tif");

    }

    /**
     * Indicates whether current path is an Android App.
     *
     * @return {@code true} if file is an Android App, {@code false} if not.
     */
    public boolean isAPKFile(File file) {
        return !file.isDirectory() && isAPKFile(file.getName());
    }

    /**
     * Indicates whether requested file path is an Android App.
     *
     * @param file File path
     * @return {@code true} if file is Android App, {@code false} if not.
     */
    public static boolean isAPKFile(String file) {
        file = file.substring(file.lastIndexOf("/") + 1);
        if (file.indexOf(".") > -1)
            file = file.substring(file.lastIndexOf(".") + 1);

        return file.equalsIgnoreCase("apk");

    }

    public boolean isArchive(File file) {
        return FileUtils.INSTANCE.getExtensionName(file.getName()).equalsIgnoreCase("zip");
    }

    /**
     * Indicates whether requested file path is an image. This is done by
     * checking Mimetype of file via {@link MimeTypes} class, and by comparing
     * file extension to a static list of extensions known to be videos.
     *
     * @return {@code true} if file is a video, {@code false} if not.
     */
    public boolean isVideoFile(File file) {
        return !file.isDirectory() && isVideoFile(file.getName());
    }

    /**
     * Indicates whether requested file path is an image. This is done by
     * checking Mimetype of file via {@link MimeTypes} class, and by comparing
     * file extension to a static list of extensions known to be videos.
     *
     * @param path
     * @return {@code true} if file is video, {@code false} if not.
     */
    public static boolean isVideoFile(String path) {
        if (MimeTypes.getInstance().getMimeType(path).startsWith("video/"))
            return true;

        String ext = path.substring(path.lastIndexOf(".") + 1);
        return ext.equalsIgnoreCase("mp4") || ext.equalsIgnoreCase("3gp")
                || ext.equalsIgnoreCase("avi") || ext.equalsIgnoreCase("webm")
                || ext.equalsIgnoreCase("m4v");
    }

    public boolean isAudioFile(File file) {
        return !file.isDirectory() && isAudioFile(file.getName());
    }

    public static boolean isAudioFile(String path) {
        if (MimeTypes.getInstance().getMimeType(path).startsWith("audio/"))
            return true;
        String ext = path.substring(path.lastIndexOf(".") + 1);
        return ext.equalsIgnoreCase("mp3") || ext.equalsIgnoreCase("wma") || ext.equalsIgnoreCase("flac")
                || ext.equalsIgnoreCase("wav") || ext.equalsIgnoreCase("aac")
                || ext.equalsIgnoreCase("ogg") || ext.equalsIgnoreCase("m4a");
    }
}
