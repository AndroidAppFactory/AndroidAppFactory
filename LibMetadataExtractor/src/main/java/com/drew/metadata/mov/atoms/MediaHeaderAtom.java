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

import java.io.IOException;

/**
 * https://developer.apple.com/library/content/documentation/QuickTime/QTFF/QTFFChap2/qtff2.html#//apple_ref/doc/uid/TP40000939-CH204-SW34
 *
 * @author Payton Garland
 */
public class MediaHeaderAtom extends FullAtom
{
    public MediaHeaderAtom(SequentialReader reader, Atom atom, QuickTimeContext context) throws IOException
    {
        super(reader, atom);

        context.creationTime = reader.getUInt32();
        context.modificationTime = reader.getUInt32();
        context.timeScale = reader.getUInt32();
        context.duration = reader.getUInt32();
        int language = reader.getUInt16();
        int quality = reader.getUInt16();
    }
}
