/**
 * Copyright (c) 1995, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.io.FilenameFilter;

public interface FileDialogPeer extends DialogPeer{
    void setFile(String file);

    void setDirectory(String dir);

    void setFilenameFilter(FilenameFilter filter);
}
