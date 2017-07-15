/**
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public abstract class ServerRequest{
    @Deprecated
    public String op_name(){
        return operation();
    }

    public String operation(){
        throw new NO_IMPLEMENT();
    }

    @Deprecated
    public void params(NVList params){
        arguments(params);
    }

    public void arguments(NVList args){
        throw new NO_IMPLEMENT();
    }

    @Deprecated
    public void result(Any any){
        set_result(any);
    }

    public void set_result(Any any){
        throw new NO_IMPLEMENT();
    }

    @Deprecated
    public void except(Any any){
        set_exception(any);
    }

    public void set_exception(Any any){
        throw new NO_IMPLEMENT();
    }

    public abstract Context ctx();
}
