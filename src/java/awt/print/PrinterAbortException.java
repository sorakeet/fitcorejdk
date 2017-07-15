/**
 * Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.print;

public class PrinterAbortException extends PrinterException{
    public PrinterAbortException(){
        super();
    }

    public PrinterAbortException(String msg){
        super(msg);
    }
}
