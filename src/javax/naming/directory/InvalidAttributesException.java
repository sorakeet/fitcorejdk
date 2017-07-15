/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.NamingException;

public class InvalidAttributesException extends NamingException{
    private static final long serialVersionUID=2607612850539889765L;

    public InvalidAttributesException(String explanation){
        super(explanation);
    }

    public InvalidAttributesException(){
        super();
    }
}
