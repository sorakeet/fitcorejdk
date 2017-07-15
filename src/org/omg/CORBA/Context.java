/**
 * Copyright (c) 1996, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public abstract class Context{
    public abstract String context_name();

    public abstract Context parent();

    public abstract Context create_child(String child_ctx_name);

    public abstract void set_one_value(String propname,Any propvalue);

    public abstract void set_values(NVList values);

    public abstract void delete_values(String propname);

    abstract public NVList get_values(String start_scope,int op_flags,
                                      String pattern);
};
