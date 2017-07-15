/**
 * Copyright (c) 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.ior.iiop;

import com.sun.corba.se.impl.encoding.CodeSetComponentInfo;
import com.sun.corba.se.spi.ior.TaggedComponent;

public interface CodeSetsComponent extends TaggedComponent{
    public CodeSetComponentInfo getCodeSetComponentInfo();
}
