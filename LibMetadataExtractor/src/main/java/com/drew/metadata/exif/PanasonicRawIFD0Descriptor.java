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
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.TagDescriptor;

import static com.drew.metadata.exif.PanasonicRawIFD0Directory.*;

/**
 * Provides human-readable string representations of tag values stored in a {@link PanasonicRawIFD0Directory}.
 *
 * @author Kevin Mott https://github.com/kwhopper
 * @author Drew Noakes https://drewnoakes.com
 */
@SuppressWarnings("WeakerAccess")
public class PanasonicRawIFD0Descriptor extends TagDescriptor<PanasonicRawIFD0Directory>
{
    public PanasonicRawIFD0Descriptor(@NotNull PanasonicRawIFD0Directory directory)
    {
        super(directory);
    }

    @Override
    @Nullable
    public String getDescription(int tagType)
    {
        switch (tagType)
        {
            case TagPanasonicRawVersion:
                return getVersionBytesDescription(TagPanasonicRawVersion, 2);
            case TagOrientation:
                return getOrientationDescription(TagOrientation);
            default:
                return super.getDescription(tagType);
        }
    }
}
