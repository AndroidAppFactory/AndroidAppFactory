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
package com.drew.metadata.exif;

import com.drew.lang.annotations.NotNull;

import java.util.HashMap;

/**
 * Describes One of several Exif directories.
 *
 * Holds information about image IFD's in a chain after the first. The first page is stored in IFD0.
 * Currently, this only applied to multi-page TIFF images
 *
 * @author Drew Noakes https://drewnoakes.com
 */
@SuppressWarnings("WeakerAccess")
public class ExifImageDirectory extends ExifDirectoryBase
{
    @NotNull
    private static final HashMap<Integer, String> _tagNameMap = new HashMap<Integer, String>();

    static
    {
        addExifTagNames(_tagNameMap);
    }

    public ExifImageDirectory()
    {
        this.setDescriptor(new ExifImageDescriptor(this));
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Exif Image";
    }

    @Override
    @NotNull
    protected HashMap<Integer, String> getTagNameMap()
    {
        return _tagNameMap;
    }
}
