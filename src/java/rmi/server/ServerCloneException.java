/**
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

public class ServerCloneException extends CloneNotSupportedException{
    private static final long serialVersionUID=6617456357664815945L;
    public Exception detail;

    public ServerCloneException(String s){
        super(s);
        initCause(null);  // Disallow subsequent initCause
    }

    public ServerCloneException(String s,Exception cause){
        super(s);
        initCause(null);  // Disallow subsequent initCause
        detail=cause;
    }

    public String getMessage(){
        if(detail==null)
            return super.getMessage();
        else
            return super.getMessage()+
                    "; nested exception is: \n\t"+
                    detail.toString();
    }

    public Throwable getCause(){
        return detail;
    }
}
