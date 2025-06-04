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

import java.io.IOException;
import java.util.ArrayList;

/**
 * https://developer.apple.com/library/content/documentation/QuickTime/QTFF/QTFFChap2/qtff2.html#//apple_ref/doc/uid/TP40000939-CH204-25691
 *
 * @author Payton Garland
 */
public abstract class SampleDescriptionAtom<T extends SampleDescription> extends FullAtom
{
    protected long numberOfEntries;
    protected ArrayList<T> sampleDescriptions;

    public SampleDescriptionAtom(SequentialReader reader, Atom atom) throws IOException
    {
        super(reader, atom);

        numberOfEntries = reader.getUInt32();

        if (numberOfEntries <= Integer.MAX_VALUE) {
            sampleDescriptions = new ArrayList<T>((int)numberOfEntries);
            for (long i = 0; i < numberOfEntries; i++) {
                sampleDescriptions.add(getSampleDescription(reader));
            }
        } else {
            // TODO surface an error here
            numberOfEntries = 0;
            sampleDescriptions = new ArrayList<T>();
        }
    }

    @NotNull
    abstract T getSampleDescription(SequentialReader reader) throws IOException;
}
