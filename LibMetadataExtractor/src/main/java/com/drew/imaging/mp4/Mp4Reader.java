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

import com.drew.lang.StreamReader;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.mp4.Mp4Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Payton Garland
 */
public class Mp4Reader
{
    private Mp4Reader() {}

    public static void extract(@NotNull InputStream inputStream, @NotNull Mp4Handler<?> handler)
    {
        StreamReader reader = new StreamReader(inputStream);
        reader.setMotorolaByteOrder(true);

        Mp4Context context = new Mp4Context();

        processBoxes(reader, -1, handler, context);
    }

    private static void processBoxes(StreamReader reader, long atomEnd, Mp4Handler<?> handler, Mp4Context context)
    {
        try {
            while (atomEnd == -1 || reader.getPosition() < atomEnd) {

                long boxSize = reader.getUInt32();

                String boxType = reader.getString(4);

                boolean isLargeSize = boxSize == 1;

                if (isLargeSize) {
                    boxSize = reader.getInt64();
                }

                if (boxSize > Integer.MAX_VALUE) {
                    handler.addError("Box size too large.");
                    break;
                }

                if (boxSize < 8) {
                    handler.addError("Box size too small.");
                    break;
                }

                // Determine if fourCC is container/atom and process accordingly.
                // Unknown atoms will be skipped

                if (handler.shouldAcceptContainer(boxType)) {
                    // Recur, to process nested boxes within container box
                    processBoxes(reader, boxSize + reader.getPosition() - 8, handler.processContainer(boxType, boxSize, context), context);
                } else if (handler.shouldAcceptBox(boxType)) {
                    handler = handler.processBox(boxType, reader.getBytes((int)boxSize - 8), boxSize, context);
                } else if (isLargeSize) {
                    if (boxSize < 16) {
                        // TODO capture this error in a directory
                        break;
                    }
                    reader.skip(boxSize - 16);
                } else {
                    reader.skip(boxSize - 8);
                }
            }
        } catch (IOException e) {
            handler.addError(e.getMessage());
        }
    }
}
