/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import sun.security.util.SecurityConstants;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class ResponseCache{
    private static ResponseCache theResponseCache;

    public synchronized static ResponseCache getDefault(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(SecurityConstants.GET_RESPONSECACHE_PERMISSION);
        }
        return theResponseCache;
    }

    public synchronized static void setDefault(ResponseCache responseCache){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(SecurityConstants.SET_RESPONSECACHE_PERMISSION);
        }
        theResponseCache=responseCache;
    }

    public abstract CacheResponse
    get(URI uri,String rqstMethod,Map<String,List<String>> rqstHeaders)
            throws IOException;

    public abstract CacheRequest put(URI uri,URLConnection conn) throws IOException;
}
