/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth;

public class RefreshFailedException extends Exception{
    private static final long serialVersionUID=5058444488565265840L;

    public RefreshFailedException(){
        super();
    }

    public RefreshFailedException(String msg){
        super(msg);
    }
}
