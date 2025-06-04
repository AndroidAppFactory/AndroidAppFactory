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

import com.drew.lang.Rational;
import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.TagDescriptor;

/**
 * Provides human-readable string representations of tag values stored in a {@link AppleMakernoteDirectory}.
 * <p>
 * Using information from https://owl.phy.queensu.ca/~phil/exiftool/TagNames/Apple.html
 *
 * @author Drew Noakes https://drewnoakes.com
 */
@SuppressWarnings("WeakerAccess")
public class AppleMakernoteDescriptor extends TagDescriptor<AppleMakernoteDirectory>
{
    public AppleMakernoteDescriptor(@NotNull AppleMakernoteDirectory directory)
    {
        super(directory);
    }

    @Override
    @Nullable
    public String getDescription(int tagType)
    {
        switch (tagType) {
            case AppleMakernoteDirectory.TAG_HDR_IMAGE_TYPE:
                return getHdrImageTypeDescription();
            case AppleMakernoteDirectory.TAG_ACCELERATION_VECTOR:
                return getAccelerationVectorDescription();
            default:
                return super.getDescription(tagType);
        }
    }

    @Nullable
    public String getHdrImageTypeDescription()
    {
        return getIndexedDescription(AppleMakernoteDirectory.TAG_HDR_IMAGE_TYPE, 3, "HDR Image", "Original Image");
    }

    @Nullable
    public String getAccelerationVectorDescription()
    {
        Rational[] values = _directory.getRationalArray(AppleMakernoteDirectory.TAG_ACCELERATION_VECTOR);
        if (values == null || values.length != 3)
            return null;
        return String.format("%.2fg %s, ", values[0].getAbsolute().doubleValue(), values[0].isPositive() ? "left" : "right") +
               String.format("%.2fg %s, ", values[1].getAbsolute().doubleValue(), values[1].isPositive() ? "down" : "up") +
               String.format("%.2fg %s",   values[2].getAbsolute().doubleValue(), values[2].isPositive() ? "forward" : "backward");
    }
}
