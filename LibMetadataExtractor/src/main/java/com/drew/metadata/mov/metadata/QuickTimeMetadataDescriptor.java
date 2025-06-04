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
package com.drew.metadata.mov.metadata;

import com.drew.metadata.mov.QuickTimeDescriptor;
import com.drew.metadata.mov.QuickTimeDirectory;

import static com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory.*;

/**
 * @author Payton Garland
 */
public class QuickTimeMetadataDescriptor extends QuickTimeDescriptor
{
    public QuickTimeMetadataDescriptor(QuickTimeDirectory directory)
    {
        super(directory);
    }

    @Override
    public String getDescription(int tagType)
    {
        switch (tagType) {
            case TAG_ARTWORK:
                return getArtworkDescription();
            case TAG_LOCATION_ROLE:
                return getLocationRoleDescription();
            default:
                return super.getDescription(tagType);
        }
    }

    private String getArtworkDescription()
    {
        return getByteLengthDescription(TAG_ARTWORK);
    }

    public String getLocationRoleDescription()
    {
        return getIndexedDescription(TAG_LOCATION_ROLE, 0,
            "Shooting location",
            "Real location",
            "Fictional location"
        );
    }
}
