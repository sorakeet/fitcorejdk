/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.tools;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import static javax.tools.JavaFileObject.Kind;

public interface JavaFileManager extends Closeable, Flushable, OptionChecker{
    ClassLoader getClassLoader(Location location);

    Iterable<JavaFileObject> list(Location location,
                                  String packageName,
                                  Set<Kind> kinds,
                                  boolean recurse)
            throws IOException;

    String inferBinaryName(Location location,JavaFileObject file);

    boolean isSameFile(FileObject a,FileObject b);

    boolean handleOption(String current,Iterator<String> remaining);

    boolean hasLocation(Location location);

    JavaFileObject getJavaFileForInput(Location location,
                                       String className,
                                       Kind kind)
            throws IOException;

    JavaFileObject getJavaFileForOutput(Location location,
                                        String className,
                                        Kind kind,
                                        FileObject sibling)
            throws IOException;

    FileObject getFileForInput(Location location,
                               String packageName,
                               String relativeName)
            throws IOException;

    FileObject getFileForOutput(Location location,
                                String packageName,
                                String relativeName,
                                FileObject sibling)
            throws IOException;

    void flush() throws IOException;

    void close() throws IOException;

    interface Location{
        String getName();

        boolean isOutputLocation();
    }
}
