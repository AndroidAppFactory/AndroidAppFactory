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
package com.drew.metadata.file;

import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Metadata;

import java.io.*;
import java.util.Date;

public class FileSystemMetadataReader
{
    public void read(@NotNull File file, @NotNull Metadata metadata) throws IOException
    {
        if (!file.isFile())
            throw new IOException("File object must reference a file");
        if (!file.exists())
            throw new IOException("File does not exist");
        if (!file.canRead())
            throw new IOException("File is not readable");

        FileSystemDirectory directory = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);

        if (directory == null) {
            directory = new FileSystemDirectory();
            metadata.addDirectory(directory);
        }

        directory.setString(FileSystemDirectory.TAG_FILE_NAME, file.getName());
        directory.setLong(FileSystemDirectory.TAG_FILE_SIZE, file.length());
        directory.setDate(FileSystemDirectory.TAG_FILE_MODIFIED_DATE, new Date(file.lastModified()));
    }
}
