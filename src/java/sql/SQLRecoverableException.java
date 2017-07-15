/**
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public class SQLRecoverableException extends SQLException{
    private static final long serialVersionUID=-4144386502923131579L;

    public SQLRecoverableException(){
        super();
    }

    public SQLRecoverableException(String reason){
        super(reason);
    }

    public SQLRecoverableException(String reason,String SQLState){
        super(reason,SQLState);
    }

    public SQLRecoverableException(String reason,String SQLState,int vendorCode){
        super(reason,SQLState,vendorCode);
    }

    public SQLRecoverableException(Throwable cause){
        super(cause);
    }

    public SQLRecoverableException(String reason,Throwable cause){
        super(reason,cause);
    }

    public SQLRecoverableException(String reason,String SQLState,Throwable cause){
        super(reason,SQLState,cause);
    }

    public SQLRecoverableException(String reason,String SQLState,int vendorCode,Throwable cause){
        super(reason,SQLState,vendorCode,cause);
    }
}
