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
package com.drew.imaging.mp4;

import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.Metadata;
import com.drew.metadata.mp4.Mp4Context;
import com.drew.metadata.mp4.Mp4Directory;

import java.io.IOException;

/**
 * @author Payton Garland
 */
public abstract class Mp4Handler<T extends Mp4Directory>
{
    @NotNull protected Metadata metadata;
    @NotNull protected T directory;

    public Mp4Handler(@NotNull Metadata metadata)
    {
        this.metadata = metadata;
        this.directory = getDirectory();
        metadata.addDirectory(directory);
    }

    @NotNull
    protected abstract T getDirectory();

    protected abstract boolean shouldAcceptBox(@NotNull String type);

    protected abstract boolean shouldAcceptContainer(@NotNull String type);

    protected abstract Mp4Handler<?> processBox(@NotNull String type, @Nullable byte[] payload, long boxSize, Mp4Context context) throws IOException;

    protected Mp4Handler<?> processContainer(@NotNull String type, long boxSize, @NotNull Mp4Context context) throws IOException
    {
        return processBox(type, null, boxSize, context);
    }

    public void addError(@NotNull String message)
    {
        directory.addError(message);
    }
}
