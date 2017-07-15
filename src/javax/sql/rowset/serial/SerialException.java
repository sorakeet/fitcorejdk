/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset.serial;

import java.sql.SQLException;

public class SerialException extends SQLException{
    static final long serialVersionUID=-489794565168592690L;

    public SerialException(){
    }

    public SerialException(String msg){
        super(msg);
    }
}
