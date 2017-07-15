/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class PartialResultException extends NamingException{
    private static final long serialVersionUID=2572144970049426786L;

    public PartialResultException(String explanation){
        super(explanation);
    }

    public PartialResultException(){
        super();
    }
}
