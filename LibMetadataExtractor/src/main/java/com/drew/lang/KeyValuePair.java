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
package com.drew.lang;

import com.drew.lang.annotations.NotNull;
import com.drew.metadata.StringValue;

/**
 * Models a key/value pair, where both are non-null {@link StringValue} objects.
 *
 * @author Drew Noakes https://drewnoakes.com
 */
public class KeyValuePair
{
    private final String _key;
    private final StringValue _value;

    public KeyValuePair(@NotNull String key, @NotNull StringValue value)
    {
        _key = key;
        _value = value;
    }

    @NotNull
    public String getKey()
    {
        return _key;
    }

    @NotNull
    public StringValue getValue()
    {
        return _value;
    }
    
    @Override
    public String toString()
    {
        return _key + ": " + _value;
    }
}
