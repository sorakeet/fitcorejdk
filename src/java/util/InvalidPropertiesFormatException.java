/**
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

import java.io.IOException;
import java.io.NotSerializableException;

public class InvalidPropertiesFormatException extends IOException{
    private static final long serialVersionUID=7763056076009360219L;

    public InvalidPropertiesFormatException(Throwable cause){
        super(cause==null?null:cause.toString());
        this.initCause(cause);
    }

    public InvalidPropertiesFormatException(String message){
        super(message);
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws NotSerializableException{
        throw new NotSerializableException("Not serializable.");
    }

    private void readObject(java.io.ObjectInputStream in)
            throws NotSerializableException{
        throw new NotSerializableException("Not serializable.");
    }
}
