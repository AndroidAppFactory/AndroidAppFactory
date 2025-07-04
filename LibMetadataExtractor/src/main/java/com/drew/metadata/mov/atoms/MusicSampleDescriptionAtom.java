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
package com.drew.metadata.mov.atoms;

import com.drew.lang.SequentialReader;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.mov.media.QuickTimeMusicDirectory;

import java.io.IOException;

/**
 * https://developer.apple.com/library/content/documentation/QuickTime/QTFF/QTFFChap3/qtff3.html#//apple_ref/doc/uid/TP40000939-CH205-57445
 *
 * @author Payton Garland
 */
public class MusicSampleDescriptionAtom extends SampleDescriptionAtom<MusicSampleDescriptionAtom.MusicSampleDescription>
{
    public MusicSampleDescriptionAtom(SequentialReader reader, Atom atom) throws IOException
    {
        super(reader, atom);
    }

    @NotNull
    @Override
    MusicSampleDescription getSampleDescription(SequentialReader reader) throws IOException
    {
        return new MusicSampleDescription(reader);
    }

    public void addMetadata(QuickTimeMusicDirectory directory)
    {
        // Do nothing
    }

    static class MusicSampleDescription extends SampleDescription
    {
        long flags;

        public MusicSampleDescription(SequentialReader reader) throws IOException
        {
            super(reader);

            flags = reader.getUInt32();
        }
    }
}
