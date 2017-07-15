/**
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

import java.awt.datatransfer.DataFlavor;
import java.io.InputStream;

public interface AccessibleStreamable{
    DataFlavor[] getMimeTypes();

    InputStream getStream(DataFlavor flavor);
}
