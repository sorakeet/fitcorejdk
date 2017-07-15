/**
 * Copyright (c) 1996, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public abstract class Request{
    public abstract Object target();

    public abstract String operation();

    public abstract NVList arguments();

    public abstract NamedValue result();

    public abstract Environment env();

    public abstract ExceptionList exceptions();

    public abstract ContextList contexts();

    public abstract Context ctx();

    public abstract void ctx(Context c);

    public abstract Any add_in_arg();

    public abstract Any add_named_in_arg(String name);

    public abstract Any add_inout_arg();

    public abstract Any add_named_inout_arg(String name);

    public abstract Any add_out_arg();

    public abstract Any add_named_out_arg(String name);

    public abstract void set_return_type(TypeCode tc);

    public abstract Any return_value();

    public abstract void invoke();

    public abstract void send_oneway();

    public abstract void send_deferred();

    public abstract boolean poll_response();

    public abstract void get_response() throws WrongTransaction;
};
