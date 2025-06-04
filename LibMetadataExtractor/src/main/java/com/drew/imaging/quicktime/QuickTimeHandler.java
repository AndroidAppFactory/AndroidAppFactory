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
package com.drew.imaging.quicktime;

import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.Metadata;
import com.drew.metadata.mov.QuickTimeContext;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.atoms.Atom;

import java.io.IOException;

/**
 * @author Payton Garland
 */
public abstract class QuickTimeHandler<T extends QuickTimeDirectory>
{
    @NotNull protected Metadata metadata;
    @NotNull protected T directory;

    public QuickTimeHandler(@NotNull Metadata metadata)
    {
        this.metadata = metadata;
        this.directory = createDirectory();
        metadata.addDirectory(directory);
    }

    @NotNull
    protected abstract T createDirectory();

    protected abstract boolean shouldAcceptAtom(@NotNull Atom atom);

    protected abstract boolean shouldAcceptContainer(@NotNull Atom atom);

    protected abstract QuickTimeHandler<?> processAtom(@NotNull Atom atom, @Nullable byte[] payload, QuickTimeContext context) throws IOException;

    protected QuickTimeHandler<?> processContainer(@NotNull Atom atom, QuickTimeContext context) throws IOException
    {
        return processAtom(atom, null, context);
    }

    public void addError(@NotNull String message)
    {
        directory.addError(message);
    }
}
