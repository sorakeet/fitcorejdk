/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

@Deprecated
abstract public class RemoteStub extends RemoteObject{
    private static final long serialVersionUID=-1585587260594494182L;

    protected RemoteStub(){
        super();
    }

    protected RemoteStub(RemoteRef ref){
        super(ref);
    }

    @Deprecated
    protected static void setRef(RemoteStub stub,RemoteRef ref){
        throw new UnsupportedOperationException();
    }
}
