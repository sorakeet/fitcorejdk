/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class LimitExceededException extends NamingException{
    private static final long serialVersionUID=-776898738660207856L;

    public LimitExceededException(){
        super();
    }

    public LimitExceededException(String explanation){
        super(explanation);
    }
}
