/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.annotation.processing;

import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;

public interface Filer{
    JavaFileObject createSourceFile(CharSequence name,
                                    Element... originatingElements) throws IOException;

    JavaFileObject createClassFile(CharSequence name,
                                   Element... originatingElements) throws IOException;

    FileObject createResource(JavaFileManager.Location location,
                              CharSequence pkg,
                              CharSequence relativeName,
                              Element... originatingElements) throws IOException;

    FileObject getResource(JavaFileManager.Location location,
                           CharSequence pkg,
                           CharSequence relativeName) throws IOException;
}
