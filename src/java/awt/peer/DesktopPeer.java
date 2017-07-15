/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public interface DesktopPeer{
    boolean isSupported(Action action);

    void open(File file) throws IOException;

    void edit(File file) throws IOException;

    void print(File file) throws IOException;

    void mail(URI mailtoURL) throws IOException;

    void browse(URI uri) throws IOException;
}
