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
package com.drew.metadata.mp4.media;

import com.drew.lang.SequentialReader;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Metadata;
import com.drew.metadata.mp4.Mp4ContainerTypes;
import com.drew.metadata.mp4.Mp4Context;
import com.drew.metadata.mp4.Mp4MediaHandler;

import java.io.IOException;

public class Mp4TextHandler extends Mp4MediaHandler<Mp4TextDirectory>
{
    public Mp4TextHandler(Metadata metadata, Mp4Context context)
    {
        super(metadata, context);
    }

    @NotNull
    @Override
    protected Mp4TextDirectory getDirectory()
    {
        return new Mp4TextDirectory();
    }

    @Override
    protected String getMediaInformation()
    {
        return Mp4ContainerTypes.BOX_MEDIA_TEXT;
    }

    @Override
    protected void processSampleDescription(@NotNull SequentialReader reader) throws IOException
    {
    }

    @Override
    protected void processMediaInformation(@NotNull SequentialReader reader) throws IOException
    {
    }

    @Override
    protected void processTimeToSample(@NotNull SequentialReader reader, Mp4Context context) throws IOException
    {
    }
}
