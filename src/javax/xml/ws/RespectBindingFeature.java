/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

public final class RespectBindingFeature extends WebServiceFeature{
    public static final String ID="javax.xml.ws.RespectBindingFeature";

    public RespectBindingFeature(){
        this.enabled=true;
    }

    public RespectBindingFeature(boolean enabled){
        this.enabled=enabled;
    }

    public String getID(){
        return ID;
    }
}
