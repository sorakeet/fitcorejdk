/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public class SQLTransactionRollbackException extends SQLTransientException{
    private static final long serialVersionUID=5246680841170837229L;

    public SQLTransactionRollbackException(){
        super();
    }

    public SQLTransactionRollbackException(String reason){
        super(reason);
    }

    public SQLTransactionRollbackException(String reason,String SQLState){
        super(reason,SQLState);
    }

    public SQLTransactionRollbackException(String reason,String SQLState,int vendorCode){
        super(reason,SQLState,vendorCode);
    }

    public SQLTransactionRollbackException(Throwable cause){
        super(cause);
    }

    public SQLTransactionRollbackException(String reason,Throwable cause){
        super(reason,cause);
    }

    public SQLTransactionRollbackException(String reason,String SQLState,Throwable cause){
        super(reason,SQLState,cause);
    }

    public SQLTransactionRollbackException(String reason,String SQLState,int vendorCode,Throwable cause){
        super(reason,SQLState,vendorCode,cause);
    }
}
