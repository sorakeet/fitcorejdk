/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class InvalidNameException extends NamingException{
    private static final long serialVersionUID=-8370672380823801105L;

    public InvalidNameException(String explanation){
        super(explanation);
    }

    public InvalidNameException(){
        super();
    }
}
