/**
 * Copyright (c) 2002, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote;

import java.io.IOException;
// imports for javadoc

public class JMXServerErrorException extends IOException{
    private static final long serialVersionUID=3996732239558744666L;
    private final Error cause;

    public JMXServerErrorException(String s,Error err){
        super(s);
        cause=err;
    }

    public Throwable getCause(){
        return cause;
    }
}
