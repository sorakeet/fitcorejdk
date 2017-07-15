/**
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.tools;

import java.io.File;
import java.io.IOException;

public interface StandardJavaFileManager extends JavaFileManager{
    boolean isSameFile(FileObject a,FileObject b);

    Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
            Iterable<? extends File> files);

    Iterable<? extends JavaFileObject> getJavaFileObjects(File... files);

    Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(
            Iterable<String> names);

    Iterable<? extends JavaFileObject> getJavaFileObjects(String... names);

    void setLocation(Location location,Iterable<? extends File> path)
            throws IOException;

    Iterable<? extends File> getLocation(Location location);
}
