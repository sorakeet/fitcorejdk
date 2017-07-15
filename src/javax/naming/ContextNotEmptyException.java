/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class ContextNotEmptyException extends NamingException{
    private static final long serialVersionUID=1090963683348219877L;

    public ContextNotEmptyException(String explanation){
        super(explanation);
    }

    public ContextNotEmptyException(){
        super();
    }
}
