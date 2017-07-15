/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.activation;

public class UnknownObjectException extends ActivationException{
    private static final long serialVersionUID=3425547551622251430L;

    public UnknownObjectException(String s){
        super(s);
    }
}
