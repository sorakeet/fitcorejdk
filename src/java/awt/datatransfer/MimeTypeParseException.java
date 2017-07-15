/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.datatransfer;

public class MimeTypeParseException extends Exception{
    // use serialVersionUID from JDK 1.2.2 for interoperability
    private static final long serialVersionUID=-5604407764691570741L;

    public MimeTypeParseException(){
        super();
    }

    public MimeTypeParseException(String s){
        super(s);
    }
} // class MimeTypeParseException
