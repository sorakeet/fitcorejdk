/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

public abstract class WebServiceFeature{
    // public static final String ID = "some unique feature Identifier";

    protected boolean enabled=false;

    protected WebServiceFeature(){
    }

    public abstract String getID();

    public boolean isEnabled(){
        return enabled;
    }
}
