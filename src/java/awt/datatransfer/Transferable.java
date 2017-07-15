/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.datatransfer;

import java.io.IOException;

public interface Transferable{
    public DataFlavor[] getTransferDataFlavors();

    public boolean isDataFlavorSupported(DataFlavor flavor);

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException;
}
