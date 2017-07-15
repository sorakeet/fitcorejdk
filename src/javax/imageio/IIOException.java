/**
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio;

import java.io.IOException;

public class IIOException extends IOException{
    public IIOException(String message){
        super(message);
    }

    public IIOException(String message,Throwable cause){
        super(message);
        initCause(cause);
    }
}
