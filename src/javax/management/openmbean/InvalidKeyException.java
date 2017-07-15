/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;

public class InvalidKeyException extends IllegalArgumentException{
    private static final long serialVersionUID=4224269443946322062L;

    public InvalidKeyException(){
        super();
    }

    public InvalidKeyException(String msg){
        super(msg);
    }
}
