/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.sasl;

import java.io.IOException;

public class SaslException extends IOException{
    private static final long serialVersionUID=4579784287983423626L;
    // Required for serialization interoperability with JSR 28
    private Throwable _exception;

    public SaslException(){
        super();
    }

    public SaslException(String detail){
        super(detail);
    }

    public SaslException(String detail,Throwable ex){
        super(detail);
        if(ex!=null){
            initCause(ex);
        }
    }

    public Throwable getCause(){
        return _exception;
    }

    public Throwable initCause(Throwable cause){
        super.initCause(cause);
        _exception=cause;
        return this;
    }

    // Override Throwable.toString() to conform to JSR 28
    public String toString(){
        String answer=super.toString();
        if(_exception!=null&&_exception!=this){
            answer+=" [Caused by "+_exception.toString()+"]";
        }
        return answer;
    }
}
