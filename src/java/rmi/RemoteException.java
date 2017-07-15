/**
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

public class RemoteException extends java.io.IOException{
    private static final long serialVersionUID=-5148567311918794206L;
    public Throwable detail;

    public RemoteException(){
        initCause(null);  // Disallow subsequent initCause
    }

    public RemoteException(String s){
        super(s);
        initCause(null);  // Disallow subsequent initCause
    }

    public RemoteException(String s,Throwable cause){
        super(s);
        initCause(null);  // Disallow subsequent initCause
        detail=cause;
    }

    public String getMessage(){
        if(detail==null){
            return super.getMessage();
        }else{
            return super.getMessage()+"; nested exception is: \n\t"+
                    detail.toString();
        }
    }

    public Throwable getCause(){
        return detail;
    }
}
