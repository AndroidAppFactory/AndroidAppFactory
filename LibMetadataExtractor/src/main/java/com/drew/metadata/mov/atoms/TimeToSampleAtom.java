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
import com.drew.metadata.mov.QuickTimeContext;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * https://developer.apple.com/library/content/documentation/QuickTime/QTFF/QTFFChap2/qtff2.html#//apple_ref/doc/uid/TP40000939-CH204-BBCGFJII
 *
 * @author Payton Garland
 */
public class TimeToSampleAtom extends FullAtom
{
    private final ArrayList<Entry> entries;

    public TimeToSampleAtom(SequentialReader reader, Atom atom) throws IOException
    {
        super(reader, atom);

        long numberOfEntries = reader.getUInt32();
        if (numberOfEntries < Integer.MAX_VALUE) {
            entries = new ArrayList<Entry>((int)numberOfEntries);
            for (int i = 0; i < numberOfEntries; i++) {
                entries.add(new Entry(reader));
            }
        } else {
            entries = new ArrayList<Entry>();
            // TODO surface this error somewhere
        }
    }

    static class Entry
    {
        long sampleCount;
        long sampleDuration;

        public Entry(SequentialReader reader) throws IOException
        {
            sampleCount = reader.getUInt32();
            sampleDuration = reader.getUInt32();
        }
    }

    public void addMetadata(QuickTimeVideoDirectory directory, QuickTimeContext context)
    {
        if (context.timeScale != null && entries.size() > 0) {
            float frameRate = (float)context.timeScale / (float)entries.get(0).sampleDuration;
            directory.setFloat(QuickTimeVideoDirectory.TAG_FRAME_RATE, frameRate);
        }
    }
}
