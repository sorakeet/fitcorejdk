/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public class SQLNonTransientException extends SQLException{
    private static final long serialVersionUID=-9104382843534716547L;

    public SQLNonTransientException(){
        super();
    }

    public SQLNonTransientException(String reason){
        super(reason);
    }

    public SQLNonTransientException(String reason,String SQLState){
        super(reason,SQLState);
    }

    public SQLNonTransientException(String reason,String SQLState,int vendorCode){
        super(reason,SQLState,vendorCode);
    }

    public SQLNonTransientException(Throwable cause){
        super(cause);
    }

    public SQLNonTransientException(String reason,Throwable cause){
        super(reason,cause);
    }

    public SQLNonTransientException(String reason,String SQLState,Throwable cause){
        super(reason,SQLState,cause);
    }

    public SQLNonTransientException(String reason,String SQLState,int vendorCode,Throwable cause){
        super(reason,SQLState,vendorCode,cause);
    }
}
