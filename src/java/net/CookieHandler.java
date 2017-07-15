/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import sun.security.util.SecurityConstants;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class CookieHandler{
    private static CookieHandler cookieHandler;

    public synchronized static CookieHandler getDefault(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(SecurityConstants.GET_COOKIEHANDLER_PERMISSION);
        }
        return cookieHandler;
    }

    public synchronized static void setDefault(CookieHandler cHandler){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(SecurityConstants.SET_COOKIEHANDLER_PERMISSION);
        }
        cookieHandler=cHandler;
    }

    public abstract Map<String,List<String>>
    get(URI uri,Map<String,List<String>> requestHeaders)
            throws IOException;

    public abstract void
    put(URI uri,Map<String,List<String>> responseHeaders)
            throws IOException;
}
