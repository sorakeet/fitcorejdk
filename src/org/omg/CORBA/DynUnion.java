/**
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

@Deprecated
public interface DynUnion extends Object, DynAny{
    public boolean set_as_default();

    public void set_as_default(boolean arg);

    public DynAny discriminator();

    public TCKind discriminator_kind();

    public DynAny member();

    public String member_name();

    public void member_name(String arg);

    public TCKind member_kind();
}
