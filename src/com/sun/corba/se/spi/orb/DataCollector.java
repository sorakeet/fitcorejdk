/**
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.orb;

import java.util.Properties;

public interface DataCollector{
    boolean isApplet();

    boolean initialHostIsLocal();

    void setParser(PropertyParser parser);

    Properties getProperties();
}
