/**
 * Copyright (c) 1996, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public abstract class NVList{
    public abstract int count();

    public abstract NamedValue add(int flags);

    public abstract NamedValue add_item(String item_name,int flags);

    public abstract NamedValue add_value(String item_name,Any val,int flags);

    public abstract NamedValue item(int index) throws Bounds;

    public abstract void remove(int index) throws Bounds;
}
