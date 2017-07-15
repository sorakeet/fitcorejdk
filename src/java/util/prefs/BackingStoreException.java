/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.prefs;

public class BackingStoreException extends Exception{
    private static final long serialVersionUID=859796500401108469L;

    public BackingStoreException(String s){
        super(s);
    }

    public BackingStoreException(Throwable cause){
        super(cause);
    }
}
