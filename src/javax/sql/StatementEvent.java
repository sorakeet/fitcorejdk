/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Created on Apr 28, 2005
 */
/**
 * Created on Apr 28, 2005
 */
package javax.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EventObject;

public class StatementEvent extends EventObject{
    static final long serialVersionUID=-8089573731826608315L;
    private SQLException exception;
    private PreparedStatement statement;

    public StatementEvent(PooledConnection con,
                          PreparedStatement statement){
        super(con);
        this.statement=statement;
        this.exception=null;
    }

    public StatementEvent(PooledConnection con,
                          PreparedStatement statement,
                          SQLException exception){
        super(con);
        this.statement=statement;
        this.exception=exception;
    }

    public PreparedStatement getStatement(){
        return this.statement;
    }

    public SQLException getSQLException(){
        return this.exception;
    }
}
