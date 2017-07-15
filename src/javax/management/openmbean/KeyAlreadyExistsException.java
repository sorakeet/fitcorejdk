/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;

public class KeyAlreadyExistsException extends IllegalArgumentException{
    private static final long serialVersionUID=1845183636745282866L;

    public KeyAlreadyExistsException(){
        super();
    }

    public KeyAlreadyExistsException(String msg){
        super(msg);
    }
}
