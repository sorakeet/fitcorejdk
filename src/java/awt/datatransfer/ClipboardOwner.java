/**
 * Copyright (c) 1996, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.datatransfer;

public interface ClipboardOwner{
    public void lostOwnership(Clipboard clipboard,Transferable contents);
}
