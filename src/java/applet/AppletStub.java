/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.applet;

import java.net.URL;

public interface AppletStub{
    boolean isActive();

    URL getDocumentBase();

    URL getCodeBase();

    String getParameter(String name);

    AppletContext getAppletContext();

    void appletResize(int width,int height);
}
