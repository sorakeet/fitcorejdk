/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.filechooser;

import java.io.File;

public abstract class FileFilter{
    public abstract boolean accept(File f);

    public abstract String getDescription();
}
