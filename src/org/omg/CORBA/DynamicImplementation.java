/**
 * Copyright (c) 1997, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

import org.omg.CORBA.portable.ObjectImpl;

@Deprecated
public class DynamicImplementation extends ObjectImpl{
    @Deprecated
    public void invoke(ServerRequest request){
        throw new NO_IMPLEMENT();
    }

    public String[] _ids(){
        throw new NO_IMPLEMENT();
    }
}
