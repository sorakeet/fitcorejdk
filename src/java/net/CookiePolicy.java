/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

public interface CookiePolicy{
    public static final CookiePolicy ACCEPT_ALL=new CookiePolicy(){
        public boolean shouldAccept(URI uri,HttpCookie cookie){
            return true;
        }
    };
    public static final CookiePolicy ACCEPT_NONE=new CookiePolicy(){
        public boolean shouldAccept(URI uri,HttpCookie cookie){
            return false;
        }
    };
    public static final CookiePolicy ACCEPT_ORIGINAL_SERVER=new CookiePolicy(){
        public boolean shouldAccept(URI uri,HttpCookie cookie){
            if(uri==null||cookie==null)
                return false;
            return HttpCookie.domainMatches(cookie.getDomain(),uri.getHost());
        }
    };

    public boolean shouldAccept(URI uri,HttpCookie cookie);
}
