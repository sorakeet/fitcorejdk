/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.NamingException;

public class InvalidSearchControlsException extends NamingException{
    private static final long serialVersionUID=-5124108943352665777L;

    public InvalidSearchControlsException(){
        super();
    }

    public InvalidSearchControlsException(String msg){
        super(msg);
    }
}
