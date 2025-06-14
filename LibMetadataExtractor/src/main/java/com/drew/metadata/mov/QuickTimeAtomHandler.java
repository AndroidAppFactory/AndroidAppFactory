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
package com.drew.metadata.mov;

import com.drew.imaging.quicktime.QuickTimeHandler;
import com.drew.lang.SequentialByteArrayReader;
import com.drew.lang.SequentialReader;
import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.Metadata;
import com.drew.metadata.mov.atoms.*;
import com.drew.metadata.mov.atoms.canon.CanonThumbnailAtom;
import com.drew.metadata.xmp.XmpReader;

import java.io.IOException;

/**
 * @author Payton Garland
 */
public class QuickTimeAtomHandler extends QuickTimeHandler<QuickTimeDirectory>
{
    private QuickTimeHandlerFactory handlerFactory = new QuickTimeHandlerFactory(this);

    public QuickTimeAtomHandler(Metadata metadata)
    {
        super(metadata);
    }

    @NotNull
    @Override
    protected QuickTimeDirectory createDirectory()
    {
        return new QuickTimeDirectory();
    }

    @Override
    public boolean shouldAcceptAtom(@NotNull Atom atom)
    {
        return atom.type.equals(QuickTimeAtomTypes.ATOM_FILE_TYPE)
            || atom.type.equals(QuickTimeAtomTypes.ATOM_MOVIE_HEADER)
            || atom.type.equals(QuickTimeAtomTypes.ATOM_HANDLER)
            || atom.type.equals(QuickTimeAtomTypes.ATOM_MEDIA_HEADER)
            || atom.type.equals(QuickTimeAtomTypes.ATOM_CANON_THUMBNAIL)
            || atom.type.equals(QuickTimeAtomTypes.ATOM_ADOBE_XMP)
            || atom.type.equals(QuickTimeAtomTypes.ATOM_TRACK_HEADER);
    }

    @Override
    public boolean shouldAcceptContainer(@NotNull Atom atom)
    {
        return atom.type.equals(QuickTimeContainerTypes.ATOM_TRACK)
            || atom.type.equals(QuickTimeContainerTypes.ATOM_USER_DATA)
            || atom.type.equals(QuickTimeContainerTypes.ATOM_METADATA)
            || atom.type.equals(QuickTimeContainerTypes.ATOM_MOVIE)
            || atom.type.equals(QuickTimeContainerTypes.ATOM_MEDIA);
    }

    @Override
    public QuickTimeHandler<?> processAtom(@NotNull Atom atom, @Nullable byte[] payload, QuickTimeContext context) throws IOException
    {
        if (payload != null) {
            SequentialReader reader = new SequentialByteArrayReader(payload);

            if (atom.type.equals(QuickTimeAtomTypes.ATOM_MOVIE_HEADER)) {
                MovieHeaderAtom movieHeaderAtom = new MovieHeaderAtom(reader, atom);
                movieHeaderAtom.addMetadata(directory);
            } else if (atom.type.equals(QuickTimeAtomTypes.ATOM_FILE_TYPE)) {
                FileTypeCompatibilityAtom fileTypeCompatibilityAtom = new FileTypeCompatibilityAtom(reader, atom);
                fileTypeCompatibilityAtom.addMetadata(directory);
            } else if (atom.type.equals(QuickTimeAtomTypes.ATOM_HANDLER)) {
                HandlerReferenceAtom handlerReferenceAtom = new HandlerReferenceAtom(reader, atom);
                return handlerFactory.getHandler(handlerReferenceAtom.getComponentType(), metadata, context);
            } else if (atom.type.equals(QuickTimeAtomTypes.ATOM_MEDIA_HEADER)) {
                new MediaHeaderAtom(reader, atom, context);
            } else if (atom.type.equals(QuickTimeAtomTypes.ATOM_CANON_THUMBNAIL)) {
                CanonThumbnailAtom canonThumbnailAtom = new CanonThumbnailAtom(reader);
                canonThumbnailAtom.addMetadata(directory);
            } else if (atom.type.equals(QuickTimeAtomTypes.ATOM_ADOBE_XMP)) {
                new XmpReader().extract(payload, metadata, directory);
            } else if (atom.type.equals(QuickTimeAtomTypes.ATOM_TRACK_HEADER)) {
                TrackHeaderAtom trackHeaderAtom = new TrackHeaderAtom(reader, atom);
                trackHeaderAtom.addMetadata(directory);
            }
        } else {
            if (atom.type.equals(QuickTimeContainerTypes.ATOM_COMPRESSED_MOVIE)) {
                directory.addError("Compressed QuickTime movies not supported");
            }
        }

        return this;
    }
}
