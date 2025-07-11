/*
 * Copyright 2002-2019 Drew Noakes and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://drewnoakes.com/code/exif/
 *    https://github.com/drewnoakes/metadata-extractor
 */
package com.drew.metadata.webp;

import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Directory;

import java.util.HashMap;

/**
 * @author Drew Noakes https://drewnoakes.com
 */
@SuppressWarnings("WeakerAccess")
public class WebpDirectory extends Directory
{
    public static final int TAG_IMAGE_HEIGHT = 1;
    public static final int TAG_IMAGE_WIDTH = 2;
    public static final int TAG_HAS_ALPHA = 3;
    public static final int TAG_IS_ANIMATION = 4;

    public static final String CHUNK_VP8X = "VP8X";
    public static final String CHUNK_VP8L = "VP8L";
    public static final String CHUNK_VP8 = "VP8 ";
    public static final String CHUNK_EXIF = "EXIF";
    public static final String CHUNK_ICCP = "ICCP";
    public static final String CHUNK_XMP = "XMP ";

    public static final String FORMAT = "WEBP";

    @NotNull
    private static final HashMap<Integer, String> _tagNameMap = new HashMap<Integer, String>();

    static {
        _tagNameMap.put(TAG_IMAGE_HEIGHT, "Image Height");
        _tagNameMap.put(TAG_IMAGE_WIDTH, "Image Width");
        _tagNameMap.put(TAG_HAS_ALPHA, "Has Alpha");
        _tagNameMap.put(TAG_IS_ANIMATION, "Is Animation");
    }

    public WebpDirectory()
    {
        this.setDescriptor(new WebpDescriptor(this));
    }

    @Override
    @NotNull
    public String getName()
    {
        return "WebP";
    }

    @Override
    @NotNull
    protected HashMap<Integer, String> getTagNameMap()
    {
        return _tagNameMap;
    }
}
