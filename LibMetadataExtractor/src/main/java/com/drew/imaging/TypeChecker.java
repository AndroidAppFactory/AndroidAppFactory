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
package com.drew.imaging;

/**
 * Used by {@link FileTypeDetector} for file types that cannot be identified by a simple byte-prefix analysis.
 */
public interface TypeChecker
{
    /**
     * Gets the number of bytes this type checker needs in order to identify its file type.
     */
    int getByteCount();

    /**
     * Returns the file type identified within 'bytes', otherwise 'Unknown'.
     */
    FileType checkType(byte[] bytes);
}
