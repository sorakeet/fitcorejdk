/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.NamingException;

public class InvalidAttributeValueException extends NamingException{
    private static final long serialVersionUID=8720050295499275011L;

    public InvalidAttributeValueException(String explanation){
        super(explanation);
    }

    public InvalidAttributeValueException(){
        super();
    }
}
