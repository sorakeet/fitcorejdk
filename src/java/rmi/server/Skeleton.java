/**
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

import java.rmi.Remote;

@Deprecated
public interface Skeleton{
    @Deprecated
    void dispatch(Remote obj,RemoteCall theCall,int opnum,long hash)
            throws Exception;

    @Deprecated
    Operation[] getOperations();
}
