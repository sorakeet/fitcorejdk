/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import java.io.IOException;

abstract public class ContentHandler{
    @SuppressWarnings("rawtypes")
    public Object getContent(URLConnection urlc,Class[] classes) throws IOException{
        Object obj=getContent(urlc);
        for(int i=0;i<classes.length;i++){
            if(classes[i].isInstance(obj)){
                return obj;
            }
        }
        return null;
    }

    abstract public Object getContent(URLConnection urlc) throws IOException;
}
