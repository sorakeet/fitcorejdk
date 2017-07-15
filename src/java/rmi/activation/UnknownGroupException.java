/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.activation;

public class UnknownGroupException extends ActivationException{
    private static final long serialVersionUID=7056094974750002460L;

    public UnknownGroupException(String s){
        super(s);
    }
}
