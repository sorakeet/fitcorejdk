/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class NoInitialContextException extends NamingException{
    private static final long serialVersionUID=-3413733186901258623L;

    public NoInitialContextException(){
        super();
    }

    public NoInitialContextException(String explanation){
        super(explanation);
    }
}
