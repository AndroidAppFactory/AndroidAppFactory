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
import com.drew.metadata.mov.QuickTimeDictionary;
import com.drew.metadata.mov.media.QuickTimeSoundDirectory;

import java.io.IOException;

/**
 * https://developer.apple.com/library/content/documentation/QuickTime/QTFF/QTFFChap3/qtff3.html#//apple_ref/doc/uid/TP40000939-CH205-BBCGGHJH
 *
 * @author Payton Garland
 */
public class SoundSampleDescriptionAtom extends SampleDescriptionAtom<SoundSampleDescriptionAtom.SoundSampleDescription>
{
    public SoundSampleDescriptionAtom(SequentialReader reader, Atom atom) throws IOException
    {
        super(reader, atom);
    }

    @NotNull
    @Override
    SoundSampleDescription getSampleDescription(SequentialReader reader) throws IOException
    {
        return new SoundSampleDescription(reader);
    }

    public void addMetadata(QuickTimeSoundDirectory directory)
    {
        if (sampleDescriptions.size() > 0) {
            // TODO why only the first one?
            SoundSampleDescription description = sampleDescriptions.get(0);

            QuickTimeDictionary.setLookup(QuickTimeSoundDirectory.TAG_AUDIO_FORMAT, description.dataFormat, directory);
            directory.setInt(QuickTimeSoundDirectory.TAG_NUMBER_OF_CHANNELS, description.numberOfChannels);
            directory.setInt(QuickTimeSoundDirectory.TAG_AUDIO_SAMPLE_SIZE, description.sampleSize);
        }
    }

    static class SoundSampleDescription extends SampleDescription
    {
        int version;
        int revisionLevel;
        int vendor;
        int numberOfChannels;
        int sampleSize;
        int compressionID;
        int packetSize;
        long sampleRate;

        public SoundSampleDescription(SequentialReader reader) throws IOException
        {
            super(reader);

            version = reader.getUInt16();
            revisionLevel = reader.getUInt16();
            vendor = reader.getInt32();
            numberOfChannels = reader.getUInt16();
            sampleSize = reader.getUInt16();
            compressionID = reader.getUInt16();
            packetSize = reader.getUInt16();
            sampleRate = reader.getUInt32();
        }
    }
}
