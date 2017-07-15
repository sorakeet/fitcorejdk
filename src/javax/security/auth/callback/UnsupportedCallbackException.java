/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.callback;

public class UnsupportedCallbackException extends Exception{
    private static final long serialVersionUID=-6873556327655666839L;
    private Callback callback;

    public UnsupportedCallbackException(Callback callback){
        super();
        this.callback=callback;
    }

    public UnsupportedCallbackException(Callback callback,String msg){
        super(msg);
        this.callback=callback;
    }

    public Callback getCallback(){
        return callback;
    }
}
