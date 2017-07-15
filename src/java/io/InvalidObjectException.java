/**
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

public class InvalidObjectException extends ObjectStreamException{
    private static final long serialVersionUID=3233174318281839583L;

    public InvalidObjectException(String reason){
        super(reason);
    }
}
