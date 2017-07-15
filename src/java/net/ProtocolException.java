/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import java.io.IOException;

public class ProtocolException extends IOException{
    private static final long serialVersionUID=-6098449442062388080L;

    public ProtocolException(String host){
        super(host);
    }

    public ProtocolException(){
    }
}
