/**
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

public class WriteAbortedException extends ObjectStreamException{
    private static final long serialVersionUID=-3326426625597282442L;
    public Exception detail;

    public WriteAbortedException(String s,Exception ex){
        super(s);
        initCause(null);  // Disallow subsequent initCause
        detail=ex;
    }

    public String getMessage(){
        if(detail==null)
            return super.getMessage();
        else
            return super.getMessage()+"; "+detail.toString();
    }

    public Throwable getCause(){
        return detail;
    }
}
