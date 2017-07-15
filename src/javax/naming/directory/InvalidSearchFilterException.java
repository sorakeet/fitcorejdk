/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.NamingException;

public class InvalidSearchFilterException extends NamingException{
    private static final long serialVersionUID=2902700940682875441L;

    public InvalidSearchFilterException(){
        super();
    }

    public InvalidSearchFilterException(String msg){
        super(msg);
    }
}
