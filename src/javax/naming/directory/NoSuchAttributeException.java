/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.NamingException;

public class NoSuchAttributeException extends NamingException{
    private static final long serialVersionUID=4836415647935888137L;

    public NoSuchAttributeException(String explanation){
        super(explanation);
    }

    public NoSuchAttributeException(){
        super();
    }
}
