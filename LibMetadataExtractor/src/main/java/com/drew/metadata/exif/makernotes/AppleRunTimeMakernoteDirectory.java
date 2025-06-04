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
package com.drew.metadata.exif.makernotes;

import java.util.HashMap;

import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Directory;

public class AppleRunTimeMakernoteDirectory extends Directory
{
    @NotNull
    protected static final HashMap<Integer, String> _tagNameMap = new HashMap<Integer, String>();

    public static final int CMTimeFlags = 1;
    public static final int CMTimeEpoch = 2;
    public static final int CMTimeScale = 3;
    public static final int CMTimeValue = 4;

    static
    {
        _tagNameMap.put(CMTimeFlags, "Flags");
        _tagNameMap.put(CMTimeEpoch, "Epoch");
        _tagNameMap.put(CMTimeScale, "Scale");
        _tagNameMap.put(CMTimeValue, "Value");
    }

    public AppleRunTimeMakernoteDirectory()
    {
        super.setDescriptor(new AppleRunTimeMakernoteDescriptor(this));
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Apple Run Time";
    }

    @Override
    @NotNull
    protected HashMap<Integer, String> getTagNameMap()
    {
        return _tagNameMap;
    }
}
