/**
 * Copyright (c) 2003, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset;

import java.sql.SQLException;

public class RowSetWarning extends SQLException{
    static final long serialVersionUID=6678332766434564774L;

    public RowSetWarning(String reason){
        super(reason);
    }

    public RowSetWarning(){
        super();
    }

    public RowSetWarning(String reason,String SQLState){
        super(reason,SQLState);
    }

    public RowSetWarning(String reason,String SQLState,int vendorCode){
        super(reason,SQLState,vendorCode);
    }

    public RowSetWarning getNextWarning(){
        SQLException warning=getNextException();
        if(warning==null||warning instanceof RowSetWarning){
            return (RowSetWarning)warning;
        }else{
            // The chained value isn't a RowSetWarning.
            // This is a programming error by whoever added it to
            // the RowSetWarning chain.  We throw a Java "Error".
            throw new Error("RowSetWarning chain holds value that is not a RowSetWarning: ");
        }
    }

    public void setNextWarning(RowSetWarning warning){
        setNextException(warning);
    }
}
