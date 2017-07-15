/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class NotContextException extends NamingException{
    private static final long serialVersionUID=849752551644540417L;

    public NotContextException(String explanation){
        super(explanation);
    }

    public NotContextException(){
        super();
    }
}
