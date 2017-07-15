/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql;

import java.sql.SQLException;

public class ConnectionEvent extends java.util.EventObject{
    static final long serialVersionUID=-4843217645290030002L;
    private SQLException ex=null;

    public ConnectionEvent(PooledConnection con){
        super(con);
    }

    public ConnectionEvent(PooledConnection con,SQLException ex){
        super(con);
        this.ex=ex;
    }

    public SQLException getSQLException(){
        return ex;
    }
}
