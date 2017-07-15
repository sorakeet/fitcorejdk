/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public class SQLTransientConnectionException extends SQLTransientException{
    private static final long serialVersionUID=-2520155553543391200L;

    public SQLTransientConnectionException(){
        super();
    }

    public SQLTransientConnectionException(String reason){
        super(reason);
    }

    public SQLTransientConnectionException(String reason,String SQLState){
        super(reason,SQLState);
    }

    public SQLTransientConnectionException(String reason,String SQLState,int vendorCode){
        super(reason,SQLState,vendorCode);
    }

    public SQLTransientConnectionException(Throwable cause){
        super(cause);
    }

    public SQLTransientConnectionException(String reason,Throwable cause){
        super(reason,cause);
    }

    public SQLTransientConnectionException(String reason,String SQLState,Throwable cause){
        super(reason,SQLState,cause);
    }

    public SQLTransientConnectionException(String reason,String SQLState,int vendorCode,Throwable cause){
        super(reason,SQLState,vendorCode,cause);
    }
}
