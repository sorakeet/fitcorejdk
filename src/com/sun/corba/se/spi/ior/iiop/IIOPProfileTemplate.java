/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.ior.iiop;

import com.sun.corba.se.spi.ior.TaggedProfileTemplate;

public interface IIOPProfileTemplate extends TaggedProfileTemplate{
    public GIOPVersion getGIOPVersion();

    public IIOPAddress getPrimaryAddress();
}
