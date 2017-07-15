/**
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

import java.security.BasicPermission;

public final class WebServicePermission extends BasicPermission{
    private static final long serialVersionUID=-146474640053770988L;

    public WebServicePermission(String name){
        super(name);
    }

    public WebServicePermission(String name,String actions){
        super(name,actions);
    }
}
