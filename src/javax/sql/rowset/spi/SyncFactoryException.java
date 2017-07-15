/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset.spi;

import java.sql.SQLException;

public class SyncFactoryException extends SQLException{
    static final long serialVersionUID=-4354595476433200352L;

    public SyncFactoryException(){
    }

    public SyncFactoryException(String msg){
        super(msg);
    }
}
