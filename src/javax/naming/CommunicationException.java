/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class CommunicationException extends NamingException{
    private static final long serialVersionUID=3618507780299986611L;

    public CommunicationException(String explanation){
        super(explanation);
    }

    public CommunicationException(){
        super();
    }
}
