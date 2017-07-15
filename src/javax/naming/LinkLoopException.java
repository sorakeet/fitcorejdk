/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class LinkLoopException extends LinkException{
    private static final long serialVersionUID=-3119189944325198009L;

    public LinkLoopException(String explanation){
        super(explanation);
    }

    public LinkLoopException(){
        super();
    }
}
