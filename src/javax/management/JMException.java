/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class JMException extends Exception{
    private static final long serialVersionUID=350520924977331825L;

    public JMException(){
        super();
    }

    public JMException(String msg){
        super(msg);
    }
}
