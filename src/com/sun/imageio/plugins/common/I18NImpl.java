/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.imageio.plugins.common;

import java.io.InputStream;
import java.util.PropertyResourceBundle;

public class I18NImpl{
    protected static final String getString(String className,String resource_name,String key){
        PropertyResourceBundle bundle=null;
        try{
            InputStream stream=
                    Class.forName(className).getResourceAsStream(resource_name);
            bundle=new PropertyResourceBundle(stream);
        }catch(Throwable e){
            throw new RuntimeException(e); // Chain the exception.
        }
        return (String)bundle.handleGetObject(key);
    }
}
