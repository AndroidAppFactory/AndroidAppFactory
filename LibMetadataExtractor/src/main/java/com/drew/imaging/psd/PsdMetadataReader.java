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

package com.drew.imaging.psd;

import com.drew.lang.StreamReader;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Metadata;
import com.drew.metadata.file.FileSystemMetadataReader;
import com.drew.metadata.photoshop.PsdReader;

import java.io.*;

/**
 * Obtains metadata from Photoshop's PSD files.
 *
 * @author Drew Noakes https://drewnoakes.com
 */
public class PsdMetadataReader
{
    @NotNull
    public static Metadata readMetadata(@NotNull File file) throws IOException
    {
        InputStream inputStream = new FileInputStream(file);
        Metadata metadata;
        try {
            metadata = readMetadata(inputStream);
        } finally {
            inputStream.close();
        }
        new FileSystemMetadataReader().read(file, metadata);
        return metadata;
    }

    @NotNull
    public static Metadata readMetadata(@NotNull InputStream inputStream)
    {
        Metadata metadata = new Metadata();
        new PsdReader().extract(new StreamReader(inputStream), metadata);
        return metadata;
    }
}
