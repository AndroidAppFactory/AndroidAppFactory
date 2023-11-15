package com.bihe0832.android.common.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.media.image.BitmapUtil;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.xmp.XmpDirectory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/9/19.
 *         Description:
 */
public class ImageMetadataUtils {

    private static final String TAG = "ImageMetadataUtils";

    public static void showAllTags(String fileName) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new File(fileName));
            Iterator metaIterator = metadata.getDirectories().iterator();
            while (true) {
                Directory directory;
                Iterator tagIterator;
                do {
                    if (!metaIterator.hasNext()) {
                        return;
                    }
                    directory = (Directory) metaIterator.next();
                    if (directory.getName().equals("XMP")) {
                        Map<String, String> stringStringMap = ((XmpDirectory) directory).getXmpProperties();
                        showDirectory(directory.getName(), stringStringMap);
                    } else {
                        tagIterator = directory.getTags().iterator();
                        while (tagIterator.hasNext()) {
                            Tag tag = (Tag) tagIterator.next();
                            ZLog.e(TAG, String.format("[%s] - %s = %s\n", directory.getName(), tag.getTagName(),
                                    tag.getDescription()));
                        }
                    }

                } while (!directory.hasErrors());
                tagIterator = directory.getErrors().iterator();
                while (tagIterator.hasNext()) {
                    String error = (String) tagIterator.next();
                    ZLog.e(TAG, String.format("ERROR: %s", error));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZLog.e(TAG, "showAllTags throw exception");
        }
    }

    public static int getXMPMicroVideoOffset(String fileName) {
        Map<String, String> stringStringMap = getXMP(fileName);
        String key = "GCamera:MicroVideoOffset";
        String offset = "";
        if (stringStringMap.containsKey(key)) {
            offset = stringStringMap.get(key);
        }
        return ConvertUtils.parseInt(offset);
    }

    public static Map<String, String> getXMP(String fileName) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new File(fileName));
            XmpDirectory directory = (XmpDirectory) metadata.getFirstDirectoryOfType(XmpDirectory.class);
            Map<String, String> stringStringMap = directory.getXmpProperties();
            return stringStringMap;
        } catch (Exception e) {
            ZLog.e(TAG, "getXMP throw exception");
        }
        return new HashMap();
    }

    public static void showDirectory(String name, Map<String, String> stringStringMap) {
        for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
            ZLog.d(TAG, String.format("[%s] - %s = %s\n", name, entry.getKey(), entry.getValue()));
        }
    }

    public static void showAPP1(String filePath) {
        // 读取 JPEG 图片的 APP1 信息
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            long app1Size = exifInterface.getAttributeInt(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, -1);
            ZLog.d(TAG, "app1: " + app1Size);
            // APP1 信息存在
            // 遍历 APP1 中的信息
            Metadata app1Metadata = ImageMetadataReader.readMetadata(new File(filePath));
            for (com.drew.metadata.Directory app1Directory : app1Metadata.getDirectories()) {
                ZLog.d(TAG, "Directory: " + app1Directory.getName());
                for (com.drew.metadata.Tag tag : app1Directory.getTags()) {
                    ZLog.d(TAG, "Tag: " + tag.getTagName() + " = " + tag.getDescription());
                }
            }
        } catch (ImageProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getImageOrientation(String imagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            return orientation;
        } catch (IOException e) {
            e.printStackTrace();
            return ExifInterface.ORIENTATION_UNDEFINED;
        }
    }

    public static int exifToTranslation(int exifOrientation) {
        int translation;
        switch (exifOrientation) {
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_VERTICAL:
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSPOSE:
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSVERSE:
                translation = -1;
                break;
            default:
                translation = 1;
        }
        return translation;
    }

    public static int exifToDegrees(int exifOrientation) {
        int rotation;
        switch (exifOrientation) {
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90:
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSPOSE:
                rotation = 90;
                break;
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180:
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_VERTICAL:
                rotation = 180;
                break;
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270:
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSVERSE:
                rotation = 270;
                break;
            default:
                rotation = 0;
        }
        return rotation;
    }

    public static void rotateImageToV(String sourceImage) {
        try {
            File file = new File(sourceImage);
            // 读取图片的 Exif 信息
            BitmapFactory.Options options = BitmapUtil.getLocalBitmapOptions(sourceImage);
            Bitmap bitmap = BitmapUtil.getLocalBitmap(sourceImage, options.outHeight, options.outWidth);
            String result = BitmapUtil.saveBitmap(ZixieContext.INSTANCE.getApplicationContext(), bitmap);
            FileUtils.INSTANCE.copyFile(new File(result), file);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
