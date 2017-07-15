/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class NameAlreadyBoundException extends NamingException{
    private static final long serialVersionUID=-8491441000356780586L;

    public NameAlreadyBoundException(String explanation){
        super(explanation);
    }

    public NameAlreadyBoundException(){
        super();
    }
}
