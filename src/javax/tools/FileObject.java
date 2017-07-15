/**
 * Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.tools;

import java.io.*;
import java.net.URI;

public interface FileObject{
    URI toUri();

    String getName();

    InputStream openInputStream() throws IOException;

    OutputStream openOutputStream() throws IOException;

    Reader openReader(boolean ignoreEncodingErrors) throws IOException;

    CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException;

    Writer openWriter() throws IOException;

    long getLastModified();

    boolean delete();
}
