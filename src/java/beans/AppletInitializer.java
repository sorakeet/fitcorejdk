/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import java.applet.Applet;
import java.beans.beancontext.BeanContext;

public interface AppletInitializer{
    void initialize(Applet newAppletBean,BeanContext bCtxt);

    void activate(Applet newApplet);
}
