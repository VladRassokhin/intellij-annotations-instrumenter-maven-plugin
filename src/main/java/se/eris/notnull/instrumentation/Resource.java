/*
 * Copyright 2013-2016 Eris IT AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.eris.notnull.instrumentation;

import org.jetbrains.annotations.Nullable;
import sun.nio.ByteBuffered;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public abstract class Resource {

    private InputStream inputStream;

    @Nullable
    public abstract InputStream getInputStream() throws IOException;

    @Nullable
    protected ByteBuffer getByteBuffer() throws IOException {
        final InputStream is = this.cachedInputStream();
        return is instanceof ByteBuffered ? ((ByteBuffered) is).getByteBuffer() : null;
    }

    @Nullable
    private synchronized InputStream cachedInputStream() throws IOException {
        if (this.inputStream == null) {
            this.inputStream = this.getInputStream();
        }

        return this.inputStream;
    }

}
