/**
 * Copyright (c) 1996, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.datatransfer;

public class UnsupportedFlavorException extends Exception{
    private static final long serialVersionUID=5383814944251665601L;

    public UnsupportedFlavorException(DataFlavor flavor){
        super((flavor!=null)?flavor.getHumanPresentableName():null);
    }
}
