/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.spec;

import java.security.GeneralSecurityException;

public class InvalidParameterSpecException extends GeneralSecurityException{
    private static final long serialVersionUID=-970468769593399342L;

    public InvalidParameterSpecException(){
        super();
    }

    public InvalidParameterSpecException(String msg){
        super(msg);
    }
}
