/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class NameNotFoundException extends NamingException{
    private static final long serialVersionUID=-8007156725367842053L;

    public NameNotFoundException(String explanation){
        super(explanation);
    }

    public NameNotFoundException(){
        super();
    }
}
