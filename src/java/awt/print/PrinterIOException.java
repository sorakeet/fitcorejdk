/**
 * Copyright (c) 1998, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.print;

import java.io.IOException;

public class PrinterIOException extends PrinterException{
    static final long serialVersionUID=5850870712125932846L;
    private IOException mException;

    public PrinterIOException(IOException exception){
        initCause(null);  // Disallow subsequent initCause
        mException=exception;
    }

    public IOException getIOException(){
        return mException;
    }

    public Throwable getCause(){
        return mException;
    }
}
