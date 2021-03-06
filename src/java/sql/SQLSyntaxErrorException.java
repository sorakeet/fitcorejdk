/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public class SQLSyntaxErrorException extends SQLNonTransientException{
    private static final long serialVersionUID=-1843832610477496053L;

    public SQLSyntaxErrorException(){
        super();
    }

    public SQLSyntaxErrorException(String reason){
        super(reason);
    }

    public SQLSyntaxErrorException(String reason,String SQLState){
        super(reason,SQLState);
    }

    public SQLSyntaxErrorException(String reason,String SQLState,int vendorCode){
        super(reason,SQLState,vendorCode);
    }

    public SQLSyntaxErrorException(Throwable cause){
        super(cause);
    }

    public SQLSyntaxErrorException(String reason,Throwable cause){
        super(reason,cause);
    }

    public SQLSyntaxErrorException(String reason,String SQLState,Throwable cause){
        super(reason,SQLState,cause);
    }

    public SQLSyntaxErrorException(String reason,String SQLState,int vendorCode,Throwable cause){
        super(reason,SQLState,vendorCode,cause);
    }
}
